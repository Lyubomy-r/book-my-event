package com.BookMyEvent.controller;

import com.BookMyEvent.entity.PromoCode;
import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;
import com.BookMyEvent.entity.dto.PaymentStatusResponseDTO;
import com.BookMyEvent.entity.dto.ProductDTO;
import com.BookMyEvent.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
@Slf4j
public class PayController {
  private final PaymentService paymentService;

  private String className = this.getClass().getSimpleName();

  @PostMapping("/{eventId}")
  public ResponseEntity<PaymentResponseDTO> prepareForPayment(@PathVariable("eventId") String eventId,
                                                              @RequestBody PaymentRequestDTO paymentRequest) {
    log.info("Class: {}, Method: prepareForPayment - get request eventId {}", className, eventId);
    PaymentResponseDTO response = paymentService.prepareForPayment(eventId, paymentRequest);
    log.info("Class: {}, Method: prepareForPayment - return {}", className, response);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/status/verification")
  public void paymentVerification(@RequestParam PaymentStatusResponseDTO payload) {
    log.info("Class: {}, Method: paymentVerification - get request about payment status verification.", className);
    paymentService.paymentVerification(payload);
  }

  @GetMapping("/price")
  @Hidden
  public ResponseEntity<Map<String, List<?>>> calculatePriceBeforeUsePromoCode(@RequestParam("productPrice") String productPrice,
                                                                               @RequestParam("productCount") String productCount) {
    log.info("Class: {}, Method: calculatePriceBeforeUsePromoCode - get request about price Info.", className);
    Map<String, List<?>> priceInfo = paymentService.calculatePriceBeforeUsePromoCode(new ProductDTO("", productPrice, productCount,""));

    log.info("pay/status  {}", priceInfo);
    return ResponseEntity.ok(priceInfo);
  }

  @GetMapping("/price/promo-code")
  @Hidden
  public ResponseEntity<Map<String, List<?>>> calculatePriceAfterUsePromoCode(@RequestParam("productPrice") String productPrice,
                                                                              @RequestParam("productCount") String productCount,
                                                                              @RequestParam("promoCode") String promoCode) {
    log.info("Class: {}, Method: calculatePriceAfterUsePromoCode - get request about price Info.", className);
    Map<String, List<?>> priceInfo = paymentService.calculatePriceAfterUsePromoCode(
        new ProductDTO("", productPrice, productCount,""),
        promoCode);

    log.info("pay/price  {}", priceInfo);
    return ResponseEntity.ok(priceInfo);
  }

  @GetMapping("/promo-code")
  public ResponseEntity<PromoCode> getPromoCode(@RequestParam("promoCode") String promoCode) {
    log.info("Class: {}, Method: getPromoCode - get request about price Info.", className);
    PromoCode promoCodeInfo = paymentService.getPromoCode(promoCode);
    log.info("Class: {}, Method: getPromoCode - return promo code info.", promoCodeInfo);

    return ResponseEntity.ok(promoCodeInfo);
  }

}
