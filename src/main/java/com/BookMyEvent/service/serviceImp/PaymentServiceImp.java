package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.OrderDetailsRepository;
import com.BookMyEvent.dao.PromoCodeRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.OrderStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.PaymentDetails;
import com.BookMyEvent.entity.PromoCode;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;
import com.BookMyEvent.entity.dto.PaymentStatusResponseDTO;
import com.BookMyEvent.entity.dto.ProductDTO;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.PaymentDetailsMapper;
import com.BookMyEvent.service.EventService;
import com.BookMyEvent.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImp implements PaymentService {

  private final PromoCodeRepository promoCodeRepository;
  private final OrderDetailsRepository orderDetailsRepository;
  private final PaymentDetailsMapper paymentDetailsMapper;
  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private final ConcurrentHashMap<ObjectId, ReentrantLock> eventLocks = new ConcurrentHashMap<>();
  private final String merchantSecretKey = "07e12edf1d5f39eaf8b1b7fd029cd10f2b557c3e";
  private final String merchantLogin = "evently_book_vercel_app";
  private final String baseUrl = "https://secure.wayforpay.com/pay";
  String merchantAccount = merchantLogin;
  String merchantDomainName = "https://evently-book.vercel.app/";
  String currency = "UAH";
  String serviceUrl = "http://localhost:8080/api/v1/pay/status/verification";

  @Value("${percentage}")
  private int percentage;

  @Override
  public PaymentResponseDTO prepareForPayment(String eventId, PaymentRequestDTO paymentRequest) {
    Event event = eventRepository.findById(new ObjectId(eventId)).orElseThrow(
        () -> new GeneralException(String.format("Event not exist by id %s", eventId), HttpStatus.NOT_FOUND));
    User user = userRepository.findById(new ObjectId(paymentRequest.userId())).orElseThrow(
        () -> new GeneralException(String.format("User not exist by id %s", paymentRequest.userId()), HttpStatus.NOT_FOUND));
    Integer availableTickets = event.getAvailableTickets() - Integer.parseInt(paymentRequest.product().productCount());
    if (availableTickets < 0) {
      log.warn("Available tickets are sold out. availableTickets {} try buy {}",
          event.getAvailableTickets(), paymentRequest.product().productCount());
      throw new GeneralException("Event is sold out. No available tickets.", HttpStatus.CONFLICT);
    }
    String orderReference = "ON" + random();
    Instant orderDateInInstant = Instant.now();
    ZoneId kyivZone = ZoneId.of("Europe/Kiev");
    ZonedDateTime kyivTime = orderDateInInstant.atZone(kyivZone);
    String orderDate = String.valueOf(kyivTime.toEpochSecond());
//    String amount = calculateAmount(paymentRequest.product(),percentage).toString();
    String dataToSign = String.join(";",
        merchantAccount,
        merchantDomainName,
        orderReference,
        orderDate,
        paymentRequest.product().amount(),
        currency,
        paymentRequest.product().productName(),
        paymentRequest.product().productCount(),
        paymentRequest.product().productPrice()
    );

    String merchantSignature = generateSignature(merchantSecretKey, dataToSign);

    log.info("merchantSignature  {}", merchantSignature);
    log.info("amount  {}", paymentRequest.product().amount());
    log.info("orderReference  {}", orderReference);
    log.info("orderDate  {}", orderDate);

    PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO(
        merchantAccount,
        "SimpleSignature",
        merchantDomainName,
        orderReference,
        orderDate,
        paymentRequest.product().amount(),
        currency,
        "600",
        "600",
        paymentRequest.product(),
        paymentRequest.clientFirstName(),
        paymentRequest.clientLastName(),
        paymentRequest.clientEmail(),
        paymentRequest.clientPhone(),
        "card",
        serviceUrl,
        merchantSignature
    );

    OrderDetails orderDetails = OrderDetails.builder()
        .orderReference(orderReference)
        .orderDate(orderDateInInstant)
        .event(event)
        .user(user)
        .paymentDetails(paymentDetailsMapper.toPaymentDetailsFromPaymentResponseDTO(paymentResponseDTO))
        .status(OrderStatus.UNPAID)
        .build();
    log.info("orderDetails  {}", orderDetails);
    orderDetailsRepository.save(orderDetails);
    return paymentResponseDTO;
  }

  @Override
  public void paymentVerification(PaymentStatusResponseDTO statusResponse) {
    if (statusResponse.transactionStatus().equals("Approved")) {
      Optional<OrderDetails> orderDetails = orderDetailsRepository.findByOrderReference(statusResponse.orderReference());
      if (orderDetails.isPresent()) {
        if (orderDetails.get().getPaymentDetails().getMerchantSignature().equals(statusResponse.merchantSignature())) {
          orderDetails.get().setPaymentDetails(
              paymentDetailsMapper.toPaymentDetails(orderDetails.get().getPaymentDetails(), statusResponse));
          orderDetails.get().setStatus(OrderStatus.PAID);

          ObjectId eventId = orderDetails.get().getEvent().getId();

          // Беремо або створюємо замок для цього eventId
          ReentrantLock lock = eventLocks.computeIfAbsent(eventId, id -> new ReentrantLock());

          lock.lock();
          try {
            Event exsistEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new GeneralException("Event not found", HttpStatus.NOT_FOUND));
            Integer productCount = Integer.parseInt(orderDetails.get().getPaymentDetails().getProduct().productCount());
            Integer productPrice = Integer.parseInt(orderDetails.get().getPaymentDetails().getProduct().productPrice());
            Integer soldTickets = exsistEvent.getSoldTickets() + productCount;
            Integer availableTickets = exsistEvent.getAvailableTickets() - productCount;
            exsistEvent.setSoldTickets(soldTickets);
            if (availableTickets < 0) {
              log.warn("Available tickets are sold out. availableTickets {} try buy {}",
                  exsistEvent.getAvailableTickets(), orderDetails.get().getPaymentDetails().getProduct().productCount());
              throw new GeneralException("Available tickets are sold out.", HttpStatus.NOT_FOUND);
            } else {
              log.info("Class: PaymentServiceImp, Method: paymentVerification.  tickets are available {}", availableTickets);
              exsistEvent.setAvailableTickets(availableTickets);
              BigDecimal calculateProfit = calculateProfit(exsistEvent.getProfit(), productPrice, productCount);
//            List<OrderDetails> orderDetailsList = orderDetailsRepository.findByEvent_Id(exsistEvent.getId());
//            BigDecimal calculateProfit = orderDetailsList.stream()
//                .map(order -> calculateAmount(order.getPaymentDetails().getProduct()))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
              exsistEvent.setProfit(calculateProfit);
            }
            Event savedEvent = eventRepository.save(exsistEvent);
            log.info("Class: PaymentServiceImp, Method: paymentVerification.  Event was saved  {}", savedEvent.getAvailableTickets());
            orderDetails.get().setEvent(savedEvent);


            OrderDetails updatedOrderDetails = orderDetailsRepository.save(orderDetails.get());
            log.info("Class: PaymentServiceImp, Method: paymentVerification.  getAvailableTickets {}", updatedOrderDetails.getEvent().getAvailableTickets());
            log.info("Class: PaymentServiceImp, Method: paymentVerification.  getSoldTickets {}", updatedOrderDetails.getEvent().getSoldTickets());
            log.info("Class: PaymentServiceImp, Method: paymentVerification.  getProfit {}", updatedOrderDetails.getEvent().getProfit());
//          log.info("Class: PaymentServiceImp, Method: paymentVerification.  updatedOrderDetails {}", updatedOrderDetails);
          } finally {
            lock.unlock();
            eventLocks.computeIfPresent(eventId, (id, l) -> {
              log.info("Class: PaymentServiceImp, Method: paymentVerification.  unlock event id {}", id);
              return l.hasQueuedThreads() ? l : null;
            });
          }
        } else {
          orderDetails.get().setPaymentDetails(paymentDetailsMapper.toPaymentDetails(
              orderDetails.get().getPaymentDetails(),
              statusResponse,
              "merchantSignature don't equals"
          ));
          log.warn("OrderDetails merchantSignature don't equals orderDetails ({}) - statusResponse ({})",
              orderDetails.get().getPaymentDetails().getMerchantSignature(), statusResponse.merchantSignature());
          orderDetailsRepository.save(orderDetails.get());
        }
      } else {
        log.warn("OrderDetails not exist by order reference {}", statusResponse.orderReference());
      }
    }
  }

  public static String generateSignature(String secretKey, String params) {
    try {
      // 1. Формуємо рядок для підпису
      String stringToSign = params;

      // 2. Створюємо HMAC_MD5 генератор
      Mac mac = Mac.getInstance("HmacMD5");
      SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacMD5");
      mac.init(secretKeySpec);

      // 3. Генеруємо підпис
      byte[] digest = mac.doFinal(stringToSign.getBytes());

      // 4. Перетворюємо підпис на 16-річне шістнадцяткове число
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }

      return sb.toString();
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      e.printStackTrace();
      return null;
    }
  }

  public long random() {
    Random random = new Random();
    long randomNumber = 1_000_000_000_000L + (long) (random.nextDouble() * 9_000_000_000_000L);
    return randomNumber;
  }

  public BigDecimal calculateAmount(ProductDTO product) {
    try {
      BigDecimal price = new BigDecimal(product.productPrice());
      BigDecimal count = new BigDecimal(product.productCount());
      return price.multiply(count).setScale(2, RoundingMode.HALF_UP);
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public BigDecimal calculateProfit(BigDecimal profits, Integer productPrice, Integer productCount) {
    try {
      BigDecimal price = new BigDecimal(productPrice);
      BigDecimal count = new BigDecimal(productCount);

      return price.multiply(count).setScale(2, RoundingMode.HALF_UP).add(profits);
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public BigDecimal calculateAmount(ProductDTO product, int servicesPercentage) {
    try {
      BigDecimal price = new BigDecimal(product.productPrice());
      BigDecimal count = new BigDecimal(product.productCount());
//      BigDecimal percentageOfPrice = price.multiply(new BigDecimal(servicesPercentage / 100));
      return price.multiply(count).setScale(2, RoundingMode.HALF_UP);
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public Map<String, List<?>> calculatePriceBeforeUsePromoCode(ProductDTO product) {
    try {
      BigDecimal calculateAmount = calculateAmount(product, percentage);

      return Map.of(
          "content", List.of(
              Map.of(
                  "amount", calculateAmount.toString(),
                  "price", product.productPrice(),
                  "promoCode", "0",
                  "collectionOfServices", new BigDecimal(product.productPrice()).multiply(new BigDecimal(percentage / 100)).toString()
              )
          )
      );
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public Map<String, List<?>> calculatePriceAfterUsePromoCode(ProductDTO product, String promoCode) {
    try {
      PromoCode exsistPromoCode = promoCodeRepository.findByName(promoCode).orElseThrow(
          () -> new GeneralException(String.format("Promo code not exist by name %s", promoCode), HttpStatus.NOT_FOUND)
      );
      int percentageAfterUsePromoCode = percentage - exsistPromoCode.getPercentage();
      BigDecimal calculateAmount = calculateAmount(product, percentageAfterUsePromoCode);

      return Map.of(
          "content", List.of(
              Map.of(
                  "amount", calculateAmount.toString(),
                  "price", product.productPrice(),
                  "promoCode", new BigDecimal(product.productPrice()).multiply(new BigDecimal(percentageAfterUsePromoCode / 100)).toString(),
                  "collectionOfServices", new BigDecimal(product.productPrice()).multiply(new BigDecimal(percentage / 100)).toString()
              )
          )
      );
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public PromoCode getPromoCode(String promoCode) {
    try {
      PromoCode exsistPromoCode = promoCodeRepository.findByName(promoCode).orElseThrow(
          () -> new GeneralException(String.format("Promo code not exist by name %s", promoCode), HttpStatus.NOT_FOUND)
      );
      return exsistPromoCode;
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}
