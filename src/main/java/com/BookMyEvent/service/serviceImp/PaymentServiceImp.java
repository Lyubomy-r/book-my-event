package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;
import com.BookMyEvent.entity.dto.ProductDTO;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImp implements PaymentService {

  private final String merchantSecretKey = "07e12edf1d5f39eaf8b1b7fd029cd10f2b557c3e";
  private final String merchantLogin = "evently_book_vercel_app";
  private final String baseUrl = "https://secure.wayforpay.com/pay";
  String merchantAccount = merchantLogin;
  String merchantDomainName = "https://evently-book.vercel.app/";
  String currency = "UAH";
  String serviceUrl = "http://localhost:8080/api/v1/pay/status";
  String orderDate = "1415379863";

  @Override
  public PaymentResponseDTO prepareForPayment(String eventId, PaymentRequestDTO paymentRequest) {
    String orderReference = "DH" + random();
    String amount = calculateAmount(paymentRequest.product()).toString();
    String dataToSign = String.join(";",
        merchantAccount,
        merchantDomainName,
        orderReference,
        orderDate,
        amount,
        currency,
        paymentRequest.product().get(0).productName(),
        paymentRequest.product().get(0).productCount(),
        paymentRequest.product().get(0).productPrice()
    );

    String merchantSignature = generateSignature2(merchantSecretKey, dataToSign);

    log.info("merchantSignature  {}", merchantSignature);
    log.info("amount  {}", amount);
    log.info("orderReference  {}", orderReference);

    PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO(
        merchantAccount,
        "SimpleSignature",
        merchantDomainName,
        orderReference,
        orderDate,
        amount,
        currency,
        "600",
        "600",
        paymentRequest.product(),
        paymentRequest.clientFirstName(),
        paymentRequest.clientLastName(),
        null,
        null,
        paymentRequest.clientEmail(),
        paymentRequest.clientPhone(),
        "card",
        serviceUrl,
        merchantSignature
    );
    return paymentResponseDTO;
  }

  public static String generateSignature2(String secretKey, String params) {
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

  public BigDecimal calculateAmount(List<ProductDTO> product) {
    try {
      return product.stream().map(prod ->BigDecimal.valueOf(Long.parseLong(prod.productPrice()) * Long.parseLong(prod.productCount()))
              .setScale(2, RoundingMode.HALF_UP))
          .reduce(BigDecimal.ZERO, BigDecimal::add);
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}
