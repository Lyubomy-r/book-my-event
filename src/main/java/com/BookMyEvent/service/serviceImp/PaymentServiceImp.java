package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.FundsRequestRepository;
import com.BookMyEvent.dao.OrderDetailsRepository;
import com.BookMyEvent.dao.PromoCodeRepository;
import com.BookMyEvent.entity.*;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.OrderStatus;
import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.dto.*;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.OrderDetailsMapper;
import com.BookMyEvent.mapper.PaymentDetailsMapper;
import com.BookMyEvent.mapper.PaymentMapper;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.PaymentService;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.BookMyEvent.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImp implements PaymentService {
  private final PromoCodeRepository promoCodeRepository;
  private final OrderDetailsRepository orderDetailsRepository;
  private final PaymentDetailsMapper paymentDetailsMapper;
  private final EventRepository eventRepository;
  private final UserService userService;
  private final MailService mailService;
  private final FundsRequestRepository fundsRequestRepository;
  private final OrderDetailsMapper orderDetailsMapper;
  private final PaymentMapper paymentMapper;
  private final ConcurrentHashMap<ObjectId, ReentrantLock> eventLocks = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ReentrantLock> eventOrderReferenceLocks =
      new ConcurrentHashMap<>();
  private final String className = this.getClass().getSimpleName();

  @Value("${merchant.secret.key}")
  private String merchantSecretKey;

  @Value("${merchant.login}")
  String merchantAccount;

  //  String merchantDomainName = "https://evently-book.vercel.app/";
  @Value("${front.url}")
  String merchantDomainName;

  String currency = "UAH";

  @Value("${service.url}")
  String serviceUrl;

  @Value("${percentage}")
  private int percentage;

  @Value("${web.user.cabinet.url}")
  private String userCabinetUrl;

  @Override
  public PaymentResponseDTO prepareForPayment(String eventId, PaymentRequestDTO paymentRequest) {
    Event event =
        eventRepository
            .findById(new ObjectId(eventId))
            .orElseThrow(
                () ->
                    new GeneralException(
                        String.format("Event not exist by id %s", eventId), HttpStatus.NOT_FOUND));
    User user = userService.findUserById(paymentRequest.userId());
    Integer availableTickets =
        event.getAvailableTickets() - Integer.parseInt(paymentRequest.product().productCount());
    if (availableTickets < 0) {
      log.warn(
          "Available tickets are sold out. availableTickets {} try buy {}",
          event.getAvailableTickets(),
          paymentRequest.product().productCount());
      throw new GeneralException("Event is sold out. No available tickets.", HttpStatus.CONFLICT);
    }
    String orderReference = "ON" + random();
    Instant orderDateInInstant = Instant.now();
    ZoneId kyivZone = ZoneId.of("Europe/Kiev");
    ZonedDateTime kyivTime = orderDateInInstant.atZone(kyivZone);
    String orderDate = String.valueOf(kyivTime.toEpochSecond());
    String dataToSign =
        String.join(
            ";",
            merchantAccount,
            merchantDomainName,
            orderReference,
            orderDate,
            paymentRequest.product().amount(),
            currency,
            paymentRequest.product().productName(),
            paymentRequest.product().productCount(),
            paymentRequest.product().productPrice());

    String merchantSignature = generateSignature(merchantSecretKey, dataToSign);

    log.info("merchantSignature  {}", merchantSignature);
    log.info("amount  {}", paymentRequest.product().amount());
    log.info("orderReference  {}", orderReference);
    log.info("orderDate  {}", orderDate);

    PaymentResponseDTO paymentResponseDto =
        new PaymentResponseDTO(
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
            merchantSignature);

    OrderDetails orderDetails =
        OrderDetails.builder()
            .orderReference(orderReference)
            .orderDate(orderDateInInstant)
            .event(event)
            .user(user)
            .paymentDetails(
                paymentDetailsMapper.toPaymentDetailsFromPaymentResponseDTO(paymentResponseDto))
            .status(OrderStatus.UNPAID)
            .build();
    log.info("orderDetails  {}", orderDetails);
    orderDetailsRepository.save(orderDetails);
    return paymentResponseDto;
  }

  @Override
  public Map<String, String> paymentVerification(PaymentStatusResponseDTO statusResponse) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

    if (statusResponse.getTransactionStatus().equals("Approved")) {
      ReentrantLock lockOrderReference =
          eventOrderReferenceLocks.computeIfAbsent(
              statusResponse.getOrderReference(), id -> new ReentrantLock());
      lockOrderReference.lock();
      try {
        Optional<OrderDetails> orderDetails =
            orderDetailsRepository.findByOrderReference(statusResponse.getOrderReference());
        if (orderDetails.isPresent()) {
          if (!orderDetails.get().getStatus().equals(OrderStatus.PAID)) {

            String str =
                statusResponse.getMerchantAccount()
                    + ";"
                    + statusResponse.getOrderReference()
                    + ";"
                    + statusResponse.getAmount()
                    + ";"
                    + statusResponse.getCurrency()
                    + ";"
                    + statusResponse.getAuthCode()
                    + ";"
                    + statusResponse.getCardPan()
                    + ";"
                    + statusResponse.getTransactionStatus()
                    + ";"
                    + statusResponse.getReasonCode();
            String getMerchantSignature = generateSignature(merchantSecretKey, str);
            if (Objects.equals(getMerchantSignature, statusResponse.getMerchantSignature())) {
              orderDetails
                  .get()
                  .setPaymentDetails(
                      paymentDetailsMapper.toPaymentDetails(
                          orderDetails.get().getPaymentDetails(), statusResponse));
              orderDetails.get().setStatus(OrderStatus.PAID);

              ObjectId eventId = orderDetails.get().getEvent().getId();

              ReentrantLock lock = eventLocks.computeIfAbsent(eventId, id -> new ReentrantLock());

              lock.lock();
              try {
                Event exsistEvent =
                    eventRepository
                        .findById(eventId)
                        .orElseThrow(
                            () -> new GeneralException("Event not found", HttpStatus.NOT_FOUND));
                Integer productCount =
                    Integer.parseInt(
                        orderDetails.get().getPaymentDetails().getProduct().productCount());
                Integer productPrice =
                    Integer.parseInt(
                        orderDetails.get().getPaymentDetails().getProduct().productPrice());
                Integer soldTickets =
                    Optional.ofNullable(exsistEvent.getSoldTickets()).orElse(0) + productCount;
                Integer availableTickets =
                    Optional.ofNullable(exsistEvent.getAvailableTickets()).orElse(0) - productCount;
                exsistEvent.setSoldTickets(soldTickets);
                if (availableTickets < 0) {
                  log.warn(
                      "Available tickets are sold out. availableTickets {} try buy {}",
                      exsistEvent.getAvailableTickets(),
                      orderDetails.get().getPaymentDetails().getProduct().productCount());
                  throw new GeneralException(
                      "Available tickets are sold out.", HttpStatus.NOT_FOUND);
                } else {
                  log.info(
                      "Class: PaymentServiceImp, Method: paymentVerification.  tickets are available {}",
                      availableTickets);
                  exsistEvent.setAvailableTickets(availableTickets);
                  BigDecimal calculateProfit =
                      calculateProfit(
                          Optional.ofNullable(exsistEvent.getProfit()).orElse(BigDecimal.ZERO),
                          productPrice,
                          productCount);
                  exsistEvent.setProfit(calculateProfit);
                }
                Event savedEvent = eventRepository.save(exsistEvent);
                log.info(
                    "Class: PaymentServiceImp, Method: paymentVerification.  Event was saved  {}",
                    savedEvent.getAvailableTickets());
                orderDetails.get().setEvent(savedEvent);

                OrderDetails updatedOrderDetails = orderDetailsRepository.save(orderDetails.get());
                log.info(
                    "Class: PaymentServiceImp, Method: paymentVerification.  getAvailableTickets {}",
                    updatedOrderDetails.getEvent().getAvailableTickets());
                log.info(
                    "Class: PaymentServiceImp, Method: paymentVerification.  getSoldTickets {}",
                    updatedOrderDetails.getEvent().getSoldTickets());
                log.info(
                    "Class: PaymentServiceImp, Method: paymentVerification.  getProfit {}",
                    updatedOrderDetails.getEvent().getProfit());
                String location =
                    updatedOrderDetails.getEvent().getEventFormat().equals(EventFormat.ONLINE)
                        ? "Online"
                        : String.format(
                            "%s, %s",
                            updatedOrderDetails.getEvent().getLocation().city(),
                            updatedOrderDetails.getEvent().getLocation().street());
                ZoneId kyivZone = ZoneId.of("Europe/Kiev");
                ZonedDateTime kyivTime =
                    ZonedDateTime.ofInstant(
                        Instant.ofEpochSecond(
                            Long.parseLong(
                                updatedOrderDetails.getPaymentDetails().getProcessingDate())),
                        kyivZone);

                mailService.sendSimpleHtmlMailMessageAfterBuyTicket(
                    orderDetails.get().getPaymentDetails().getClientEmail(),
                    String.format(
                        "Дякую! Замовлення оплачено: %s",
                        updatedOrderDetails.getPaymentDetails().getProduct().productName()),
                    updatedOrderDetails.getPaymentDetails().getClientFirstName().toUpperCase(),
                    updatedOrderDetails.getPaymentDetails().getProduct().productName(),
                    String.format(
                        "%s, %s",
                        updatedOrderDetails.getEvent().getDate().day(),
                        updatedOrderDetails.getEvent().getDate().time()),
                    location,
                    updatedOrderDetails.getOrderReference(),
                    kyivTime.toLocalDate().toString(),
                    String.format(
                        "%02d:%02d,",
                        kyivTime.toLocalTime().getHour(), kyivTime.toLocalTime().getMinute()),
                    String.format(
                        "%s грн.", updatedOrderDetails.getPaymentDetails().getProduct().amount()),
                    savedEvent.getImages().get(0),
                    userCabinetUrl);
              } finally {
                lock.unlock();
                eventLocks.computeIfPresent(
                    eventId,
                    (id, l) -> {
                      log.info(
                          "Class: PaymentServiceImp, Method: paymentVerification.  unlock event id {}",
                          id);
                      return l.hasQueuedThreads() ? l : null;
                    });
              }

            } else {
              orderDetails
                  .get()
                  .setPaymentDetails(
                      paymentDetailsMapper.toPaymentDetails(
                          orderDetails.get().getPaymentDetails(),
                          statusResponse,
                          "merchantSignature don't equals"));
              log.warn(
                  "OrderDetails merchantSignature don't equals orderDetails ({}) - statusResponse ({})",
                  orderDetails.get().getPaymentDetails().getMerchantSignature(),
                  statusResponse.getMerchantSignature());
              orderDetailsRepository.save(orderDetails.get());
            }
            Long instantNow = Instant.now().getEpochSecond();
            String str2 = statusResponse.getOrderReference() + ";" + "accept" + ";" + instantNow;
            String getMerchantSignatureResponse = generateSignature(merchantSecretKey, str2);
            return Map.of(
                "orderReference",
                statusResponse.getOrderReference(),
                "status",
                "accept",
                "time",
                instantNow.toString(),
                "signature",
                getMerchantSignatureResponse);
          }
          log.info(
              "{}::{} - finishe payment verification status {} order {}",
              className,
              methodName,
              "Approved",
              statusResponse.getOrderReference());
          Long instantNow = Instant.now().getEpochSecond();
          String str2 = statusResponse.getOrderReference() + ";" + "accept" + ";" + instantNow;
          String getMerchantSignatureResponse = generateSignature(merchantSecretKey, str2);
          return Map.of(
              "orderReference",
              statusResponse.getOrderReference(),
              "status",
              "accept",
              "time",
              instantNow.toString(),
              "signature",
              getMerchantSignatureResponse);
        } else {
          log.warn(
              "OrderDetails not exist by order reference {}", statusResponse.getOrderReference());
        }
      } finally {
        lockOrderReference.unlock();
        eventOrderReferenceLocks.computeIfPresent(
            statusResponse.getOrderReference(),
            (id, l) -> {
              log.info(
                  "Class: PaymentServiceImp, Method: paymentVerification.  unlock event id {}", id);
              return l.hasQueuedThreads() ? l : null;
            });
      }
    } else if (statusResponse.getTransactionStatus().equals("Refunded")) {
      Optional<OrderDetails> orderDetails =
          orderDetailsRepository.findByOrderReference(statusResponse.getOrderReference());
      if (orderDetails.isPresent()) {
        if (!orderDetails.get().getStatus().equals(OrderStatus.REFUNDED)) {
          String str =
              statusResponse.getMerchantAccount()
                  + ";"
                  + statusResponse.getOrderReference()
                  + ";"
                  + statusResponse.getAmount()
                  + ";"
                  + statusResponse.getCurrency()
                  + ";"
                  + statusResponse.getAuthCode()
                  + ";"
                  + statusResponse.getCardPan()
                  + ";"
                  + statusResponse.getTransactionStatus()
                  + ";"
                  + statusResponse.getReasonCode();
          String getMerchantSignature = generateSignature(merchantSecretKey, str);
          if (Objects.equals(getMerchantSignature, statusResponse.getMerchantSignature())) {
            orderDetails
                .get()
                .setPaymentDetails(
                    paymentDetailsMapper.toPaymentDetails(
                        orderDetails.get().getPaymentDetails(), statusResponse));
            orderDetails.get().setStatus(OrderStatus.REFUNDED);

            ObjectId eventId = orderDetails.get().getEvent().getId();

            ReentrantLock lock = eventLocks.computeIfAbsent(eventId, id -> new ReentrantLock());

            lock.lock();
            try {
              Event exsistEvent =
                  eventRepository
                      .findById(eventId)
                      .orElseThrow(
                          () -> new GeneralException("Event not found", HttpStatus.NOT_FOUND));
              Integer productCount =
                  Integer.parseInt(
                      orderDetails.get().getPaymentDetails().getProduct().productCount());
              Integer productPrice =
                  Integer.parseInt(
                      orderDetails.get().getPaymentDetails().getProduct().productPrice());
              Double productAmount =
                  Double.parseDouble(orderDetails.get().getPaymentDetails().getProduct().amount());
              Integer soldTickets =
                  Optional.ofNullable(exsistEvent.getSoldTickets()).orElse(0) - productCount;
              Integer availableTickets =
                  Optional.ofNullable(exsistEvent.getAvailableTickets()).orElse(0) + productCount;
              exsistEvent.setSoldTickets(soldTickets);
              if (availableTickets < 0) {
                log.warn(
                    "Available tickets are sold out. availableTickets {} try buy {}",
                    exsistEvent.getAvailableTickets(),
                    orderDetails.get().getPaymentDetails().getProduct().productCount());
                throw new GeneralException("Available tickets are sold out.", HttpStatus.NOT_FOUND);
              } else {
                log.info(
                    "Class: PaymentServiceImp, Method: paymentVerification.  tickets are available {}",
                    availableTickets);
                exsistEvent.setAvailableTickets(availableTickets);
                BigDecimal calculateProfit =
                    exsistEvent.getProfit().subtract(BigDecimal.valueOf(productAmount));
                exsistEvent.setProfit(calculateProfit);
              }
              Event savedEvent = eventRepository.save(exsistEvent);
              log.info(
                  "Class: PaymentServiceImp, Method: paymentVerification.  Event was saved  {}",
                  savedEvent.getAvailableTickets());
              orderDetails.get().setEvent(savedEvent);

              OrderDetails updatedOrderDetails = orderDetailsRepository.save(orderDetails.get());
              log.info(
                  "Class: PaymentServiceImp, Method: paymentVerification.  getAvailableTickets {}",
                  updatedOrderDetails.getEvent().getAvailableTickets());
              log.info(
                  "Class: PaymentServiceImp, Method: paymentVerification.  getSoldTickets {}",
                  updatedOrderDetails.getEvent().getSoldTickets());
              log.info(
                  "Class: PaymentServiceImp, Method: paymentVerification.  getProfit {}",
                  updatedOrderDetails.getEvent().getProfit());
              //          log.info("Class: PaymentServiceImp, Method: paymentVerification.
              // updatedOrderDetails {}", updatedOrderDetails);
            } finally {
              lock.unlock();
              eventLocks.computeIfPresent(
                  eventId,
                  (id, l) -> {
                    log.info(
                        "Class: PaymentServiceImp, Method: paymentVerification.  unlock event id {}",
                        id);
                    return l.hasQueuedThreads() ? l : null;
                  });
            }
          } else {
            orderDetails
                .get()
                .setPaymentDetails(
                    paymentDetailsMapper.toPaymentDetails(
                        orderDetails.get().getPaymentDetails(),
                        statusResponse,
                        "merchantSignature don't equals"));
            log.warn(
                "OrderDetails merchantSignature don't equals orderDetails ({}) - statusResponse ({})",
                orderDetails.get().getPaymentDetails().getMerchantSignature(),
                statusResponse.getMerchantSignature());
            orderDetailsRepository.save(orderDetails.get());
          }
          Long instantNow = Instant.now().getEpochSecond();
          String str2 = statusResponse.getOrderReference() + ";" + "accept" + ";" + instantNow;
          String getMerchantSignatureResponse = generateSignature(merchantSecretKey, str2);
          return Map.of(
              "orderReference",
              statusResponse.getOrderReference(),
              "status",
              "accept",
              "time",
              instantNow.toString(),
              "signature",
              getMerchantSignatureResponse);
        }
        log.info(
            "{}::{} - finishe payment verification status {} order {}",
            className,
            methodName,
            "Refunded",
            statusResponse.getOrderReference());
        Long instantNow = Instant.now().getEpochSecond();
        String str2 = statusResponse.getOrderReference() + ";" + "accept" + ";" + instantNow;
        String getMerchantSignatureResponse = generateSignature(merchantSecretKey, str2);
        return Map.of(
            "orderReference",
            statusResponse.getOrderReference(),
            "status",
            "accept",
            "time",
            instantNow.toString(),
            "signature",
            getMerchantSignatureResponse);
      } else {
        log.warn(
            "OrderDetails not exist by order reference {}", statusResponse.getOrderReference());
      }
    }
    log.warn(
        "OrderDetails status Response getOrderReference {}, status {} ,Response object{}",
        statusResponse.getOrderReference(),
        statusResponse.getTransactionStatus(),
        statusResponse);
    Long instantNow = Instant.now().getEpochSecond();
    String str2 = statusResponse.getOrderReference() + ";" + "accept" + ";" + instantNow;
    String getMerchantSignatureResponse = generateSignature(merchantSecretKey, str2);
    return Map.of(
        "orderReference",
        statusResponse.getOrderReference(),
        "status",
        "accept",
        "time",
        instantNow.toString(),
        "signature",
        getMerchantSignatureResponse);
  }

  @Override
  public OrderDetailsDto paymentVerificationFreeEvents(
      String eventId, PaymentRequestDTO paymentRequest) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    ObjectId objectId = new ObjectId(eventId);
    ReentrantLock lock = eventLocks.computeIfAbsent(objectId, id -> new ReentrantLock());

    lock.lock();
    try {
      Event exsistEvent =
          eventRepository
              .findById(objectId)
              .orElseThrow(() -> new GeneralException("Event not found", HttpStatus.NOT_FOUND));
      if (exsistEvent.getTicketPrice() != 0) {
        log.warn("{}::{} - Send error massage.", className, methodName);
        throw new GeneralException(
            "The event is not free the server cannot process the request.", HttpStatus.CONFLICT);
      }
      Integer productCount = Integer.parseInt(paymentRequest.product().productCount());
      Integer soldTickets =
          Optional.ofNullable(exsistEvent.getSoldTickets()).orElse(0) + productCount;
      Integer availableTickets =
          Optional.ofNullable(exsistEvent.getAvailableTickets()).orElse(0) - productCount;
      if (!exsistEvent.getUnlimitedTickets()) {
        if (availableTickets < 0) {
          log.warn(
              "{}::{} - Available tickets are sold out. availableTickets {} try buy {}",
              className,
              methodName,
              exsistEvent.getAvailableTickets(),
              paymentRequest.product().productCount());
          throw new GeneralException("Available tickets are sold out.", HttpStatus.NOT_FOUND);
        } else {
          log.info("{}::{} - tickets are available {}", className, methodName, availableTickets);
          exsistEvent.setAvailableTickets(availableTickets);
          exsistEvent.setSoldTickets(soldTickets);
        }
      } else {
        log.info("{}::{} - tickets sold {}", className, methodName, soldTickets);
        exsistEvent.setSoldTickets(soldTickets);
      }
      Event savedEvent = eventRepository.save(exsistEvent);
      log.info(
          "{}::{} - Event was saved  {}", className, methodName, savedEvent.getAvailableTickets());
      User user = userService.findUserById(paymentRequest.userId());
      String orderReference = "ON" + random();
      Instant orderDateInInstant = Instant.now();
      PaymentDetails paymentDetails = paymentMapper.toPaymentDetailsForFreePurchase(paymentRequest);
      log.info("{}::{} - paymentDetails was created  {}", className, methodName, paymentDetails);
      //          PaymentDetails.builder()
      //              .product(paymentRequest.product())
      //              .clientFirstName(paymentRequest.clientFirstName())
      //              .clientLastName(paymentRequest.clientLastName())
      //              .clientEmail(paymentRequest.clientEmail())
      //              .clientPhone(paymentRequest.clientPhone())
      //              .transactionStatus("Approved")
      //              .reason("Ok")
      //              .build();
      OrderDetails orderDetails =
          orderDetailsMapper.toOrderDetailsForFreePurchase(
              orderReference, orderDateInInstant, savedEvent, user, paymentDetails);
      //          OrderDetails.builder()
      //              .orderReference(orderReference)
      //              .orderDate(orderDateInInstant)
      //              .event(savedEvent)
      //              .user(user)
      //              .paymentDetails(paymentDetails)
      //              .status(OrderStatus.PAID)
      //              .build();
      log.info("{}::{} - Created order details {}", className, methodName, orderDetails);
      OrderDetails savedOrderDetails = orderDetailsRepository.save(orderDetails);
      log.info(
          "{}::{} - get available tickets {} / get sold tickets {} / get profit {}, after saved order.",
          className,
          methodName,
          savedOrderDetails.getEvent().getAvailableTickets(),
          savedOrderDetails.getEvent().getSoldTickets(),
          savedOrderDetails.getEvent().getProfit());

      String location =
          savedOrderDetails.getEvent().getEventFormat().equals(EventFormat.ONLINE)
              ? "Online"
              : String.format(
                  "%s, %s",
                  savedOrderDetails.getEvent().getLocation().city(),
                  savedOrderDetails.getEvent().getLocation().street());
      ZoneId kyivZone = ZoneId.of("Europe/Kiev");
      ZonedDateTime kyivTime =
          ZonedDateTime.ofInstant(
              Instant.now(),
              kyivZone);
      mailService.sendSimpleHtmlMailMessageAfterBuyTicket(
          paymentRequest.clientEmail(),
          String.format(
              "Дякую! Замовлення оплачено: %s",
              savedOrderDetails.getPaymentDetails().getProduct().productName()),
          savedOrderDetails.getPaymentDetails().getClientFirstName().toUpperCase(),
          savedOrderDetails.getPaymentDetails().getProduct().productName(),
          String.format(
              "%s, %s",
              savedOrderDetails.getEvent().getDate().day(),
              savedOrderDetails.getEvent().getDate().time()),
          location,
          savedOrderDetails.getOrderReference(),
          kyivTime.toLocalDate().toString(),
          String.format(
              "%02d:%02d.", kyivTime.toLocalTime().getHour(), kyivTime.toLocalTime().getMinute()),
"",
//          String.format("%s грн.", Optional.ofNullable(savedOrderDetails.getPaymentDetails().getProduct().amount()).orElse(String.valueOf(0))),
          savedEvent.getImages().get(0),
          userCabinetUrl);

      return orderDetailsMapper.newOrderDetails(savedOrderDetails);
    } finally {
      lock.unlock();
      eventLocks.computeIfPresent(
          objectId,
          (id, l) -> {
            log.info("{}::{} - unlock event id {}", className, methodName, id);
            return l.hasQueuedThreads() ? l : null;
          });
    }
  }

  public OrderDetails findByOrderReference(String orderReference) {
    OrderDetails orderDetails =
        orderDetailsRepository
            .findByOrderReference(orderReference)
            .orElseThrow(
                () -> {
                  log.warn("OrderDetails not exist by order reference {}", orderReference);
                  throw new GeneralException(
                      String.format(
                          "OrderDetails not found by order reference %s.", orderReference),
                      HttpStatus.NOT_FOUND);
                });

    return orderDetails;
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

  public BigDecimal calculateAmount(ProductDTO product, int servicesPercentage) {
    try {
      BigDecimal price = new BigDecimal(product.productPrice());
      BigDecimal count = new BigDecimal(product.productCount());
      //      BigDecimal percentageOfPrice = price.multiply(new BigDecimal(servicesPercentage /
      // 100));
      return price.multiply(count).setScale(2, RoundingMode.HALF_UP);
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public BigDecimal calculateProfit(
      BigDecimal profits, Integer productPrice, Integer productCount) {
    try {
      BigDecimal price = new BigDecimal(productPrice);
      BigDecimal count = new BigDecimal(productCount);

      return price.multiply(count).setScale(2, RoundingMode.HALF_UP).add(profits);
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public Map<String, List<?>> calculatePriceBeforeUsePromoCode(ProductDTO product) {
    try {
      BigDecimal calculateAmount = calculateAmount(product, percentage);

      return Map.of(
          "content",
          List.of(
              Map.of(
                  "amount", calculateAmount.toString(),
                  "price", product.productPrice(),
                  "promoCode", "0",
                  "collectionOfServices",
                      new BigDecimal(product.productPrice())
                          .multiply(new BigDecimal(percentage / 100))
                          .toString())));
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public Map<String, List<?>> calculatePriceAfterUsePromoCode(
      ProductDTO product, String promoCode) {
    try {
      PromoCode exsistPromoCode =
          promoCodeRepository
              .findByName(promoCode)
              .orElseThrow(
                  () ->
                      new GeneralException(
                          String.format("Promo code not exist by name %s", promoCode),
                          HttpStatus.NOT_FOUND));
      int percentageAfterUsePromoCode = percentage - exsistPromoCode.getPercentage();
      BigDecimal calculateAmount = calculateAmount(product, percentageAfterUsePromoCode);

      return Map.of(
          "content",
          List.of(
              Map.of(
                  "amount", calculateAmount.toString(),
                  "price", product.productPrice(),
                  "promoCode",
                      new BigDecimal(product.productPrice())
                          .multiply(new BigDecimal(percentageAfterUsePromoCode / 100))
                          .toString(),
                  "collectionOfServices",
                      new BigDecimal(product.productPrice())
                          .multiply(new BigDecimal(percentage / 100))
                          .toString())));
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public PromoCode getPromoCode(String promoCode) {
    PromoCode exsistPromoCode =
        promoCodeRepository
            .findByName(promoCode)
            .orElseThrow(
                () ->
                    new GeneralException(
                        String.format("Promo code not exist by name %s", promoCode),
                        HttpStatus.NOT_FOUND));
    log.info(
        "Class: PaymentServiceImp, Method: getPromoCode.  return exsist promo code {}",
        exsistPromoCode);

    return exsistPromoCode;
  }

  @Override
  public String saveFundsRequest(String userId, CreateFundsRequestDTO fundsRequest) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    User user = userService.findUserById(userId);
    List<Event> events = eventRepository.findByOrganizers_IdAndIsCompleted(user.getId(), true);
    log.info("events list size {}", events.size());
    List<FundsRequest> fundsRequests =
        fundsRequestRepository.findByUserIdAndStatusIn(
            user.getId().toHexString(), List.of(FundsStatus.PENDING, FundsStatus.COMPLETED));
    Set<String> alreadyFundsRequestsEventId =
        fundsRequests.stream()
            .flatMap(funds -> funds.getEventIds().stream())
            .collect(Collectors.toSet());
    List<Event> newEventsToFundsRequest =
        events.stream()
            .filter(event -> !alreadyFundsRequestsEventId.contains(event.getId().toHexString()))
            .toList();
    if (newEventsToFundsRequest.isEmpty()) {
      log.warn("{}::{} - Return error message.", className, methodName);
      throw new GeneralException("No funds available for withdrawal", HttpStatus.CONFLICT);
    }
    BigDecimal totalAvailable =
        newEventsToFundsRequest.stream()
            .map(Event::getProfit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalAvailable.compareTo(fundsRequest.getAmount()) == 0) {
      FundsRequest newFundsRequest =
          FundsRequest.builder()
              .amount(fundsRequest.getAmount())
              .cartNumber(fundsRequest.getCartNumber())
              .creationDate(Instant.now())
              .status(FundsStatus.PENDING)
              .userId(user.getId().toHexString())
              .eventIds(
                  newEventsToFundsRequest.stream()
                      .map(event -> event.getId().toHexString())
                      .toList())
              .build();
      fundsRequestRepository.save(newFundsRequest);
      log.info("{}::{} - return successfully message.", className, methodName);
      return "The Funds request saved successfully.";
    } else {
      log.warn("{}::{} - Return error message.", className, methodName);
      throw new GeneralException(
          "Request amount is bigger than available amount. Withdrawals are permitted only for the full account balance and may not be made in partial amounts.",
          HttpStatus.CONFLICT);
    }
  }

  @Override
  public BigDecimal getOrganizerFunds(String userId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    User user = userService.findUserById(userId);
    List<Event> events = eventRepository.findByOrganizers_IdAndIsCompleted(user.getId(), true);
    log.info("events list size {}", events.size());
    List<FundsRequest> fundsRequests =
        fundsRequestRepository.findByUserIdAndStatusIn(
            user.getId().toHexString(), List.of(FundsStatus.PENDING, FundsStatus.COMPLETED));
    Set<String> alreadyFundsRequestsEventId =
        fundsRequests.stream()
            .flatMap(funds -> funds.getEventIds().stream())
            .collect(Collectors.toSet());
    List<Event> newEventsToFundsRequest =
        events.stream()
            .filter(event -> !alreadyFundsRequestsEventId.contains(event.getId().toHexString()))
            .toList();
    if (newEventsToFundsRequest.isEmpty()) {
      log.info("{}::{} - Return available balance (balance is empty).", className, methodName);
      return BigDecimal.ZERO;
    }
    BigDecimal totalAvailable =
        newEventsToFundsRequest.stream()
            .map(Event::getProfit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    log.info("{}::{} - Return available balance.", className, methodName);
    return totalAvailable;
  }

  @Override
  public BigDecimal getOrganizerWithdrawnFunds(String userId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    User user = userService.findUserById(userId);
    List<Event> events = eventRepository.findByOrganizers_IdAndIsCompleted(user.getId(), true);
    log.info("events list size {}", events.size());
    List<FundsRequest> fundsRequests =
        fundsRequestRepository.findByUserIdAndStatusIn(
            user.getId().toHexString(), List.of(FundsStatus.COMPLETED));
    Set<String> alreadyFundsRequestsEventId =
        fundsRequests.stream()
            .flatMap(funds -> funds.getEventIds().stream())
            .collect(Collectors.toSet());
    List<Event> completedEventsToFundsRequest =
        events.stream()
            .filter(event -> alreadyFundsRequestsEventId.contains(event.getId().toHexString()))
            .toList();
    if (completedEventsToFundsRequest.isEmpty()) {
      log.info("{}::{} - Return withdrawn balance (balance is empty).", className, methodName);
      return BigDecimal.ZERO;
    }
    BigDecimal totalAvailable =
        completedEventsToFundsRequest.stream()
            .map(Event::getProfit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    log.info("{}::{} - Return withdrawn balance.", className, methodName);
    return totalAvailable;
  }

  //  public Map<String, String> getEventFungAfter( List<Event> events, List<FundsRequest>
  // fundsRequests){
  //    Map<String, String> objectMap  = new HashMap<>();
  //    Set<String> alreadyFundsRequestsEventId =
  //            fundsRequests.stream()
  //                    .flatMap(funds -> funds.getEventIds().stream())
  //                    .collect(Collectors.toSet());
  //            events
  //            .forEach(event -> {
  //              if(alreadyFundsRequestsEventId.contains(event.getId().toHexString())){
  //               BigDecimal eventProfit =  event.getProfit();
  //                BigDecimal fundsReq = fundsRequests.stream()
  //                        .filter(funs-> isInList(funs, events) )
  //                        .map(FundsRequest::getAmount)
  //                        .reduce(BigDecimal.ZERO, BigDecimal::add);
  //                BigDecimal last = eventProfit.subtract(fundsReq, new MathContext(1,
  // RoundingMode.HALF_UP));
  //                if(last.compareTo(BigDecimal.ZERO)>0){
  //                  objectMap.put(event.getId().toHexString(), last.toString());
  //                }
  //
  //              }else{
  //                objectMap.put(event.getId().toHexString(), event.getProfit().toString());
  //              }
  //            });
  //    return objectMap;
  //  }
  //
  //  public Boolean isInList(FundsRequest request, List<Event> events) {
  //
  //    List <Event> eventList = events.stream()
  //            .filter(event -> request.getEventIds().contains(event.getId().toHexString() ) )
  //            .toList();
  //    return eventList.isEmpty();
  //  }

}
