package com.BookMyEvent.controller;

import com.BookMyEvent.entity.FundsRequest;
import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.PromoCode;
import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;
import com.BookMyEvent.entity.dto.PaymentStatusResponseDTO;
import com.BookMyEvent.entity.dto.ProductDTO;
import com.BookMyEvent.entity.dto.WayforpayRequest;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.PaymentService;
import com.fasterxml.jackson.core.util.RequestPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
@Slf4j
public class PayController {
  private final PaymentService paymentService;
  private final RestTemplate restTemplate = new RestTemplate();

  private String className = this.getClass().getSimpleName();

  @PostMapping("/{eventId}")
  public ResponseEntity<PaymentResponseDTO> prepareForPayment(
      @PathVariable("eventId") String eventId, @RequestBody PaymentRequestDTO paymentRequest) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("Class: {}, Method: {} - get request eventId {}", className, methodName, eventId);
    PaymentResponseDTO response = paymentService.prepareForPayment(eventId, paymentRequest);
    log.info("Class: {}, Method: prepareForPayment - return {}", className, response);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/free/{eventId}")
  public ResponseEntity<AppResponse> paymentVerificationFreeEvents(
      @PathVariable("eventId") String eventId, @RequestBody PaymentRequestDTO paymentRequest) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("Class: {}, Method: {} - get request eventId {}", className, methodName, eventId);
    String responseMessage = paymentService.paymentVerificationFreeEvents(eventId, paymentRequest);
    AppResponse response = new AppResponse(200, responseMessage);
    log.info("Class: {}, Method: {} - return {}", className, methodName, response);
    return ResponseEntity.ok(response);
  }

  @PostMapping(
      value = "/status/verification",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Map<String, String>> paymentVerification(HttpServletRequest request)
      throws IOException {
    String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    log.info("Class: {}, Method: paymentVerification - raw body: {}", className, body);

    ObjectMapper objectMapper = new ObjectMapper();
    PaymentStatusResponseDTO payload = objectMapper.readValue(body, PaymentStatusResponseDTO.class);
    log.info(
        "Class: {}, Method: paymentVerification - get request about payment status verification.",
        className);
    log.info(
        "Class: {}, Method: paymentVerification - get request about payment status verification. {}",
        className,
        payload);

    Map<String, String> response = paymentService.paymentVerification(payload);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/order/details/{orderReference}")
  public ResponseEntity<OrderDetails> getOrderDetails(
      @PathVariable("orderReference") String orderReference) {
    log.info("Class: {}, Method: getOrderDetails - get request about order details.", className);
    OrderDetails orderDetails = paymentService.findByOrderReference(orderReference);

    log.info("Class: {}, Method: getOrderDetails - return info about order details.", className);
    return ResponseEntity.ok(orderDetails);
  }

  @GetMapping("/price")
  @Hidden
  public ResponseEntity<Map<String, List<?>>> calculatePriceBeforeUsePromoCode(
      @RequestParam("productPrice") String productPrice,
      @RequestParam("productCount") String productCount) {
    log.info(
        "Class: {}, Method: calculatePriceBeforeUsePromoCode - get request about price Info.",
        className);
    Map<String, List<?>> priceInfo =
        paymentService.calculatePriceBeforeUsePromoCode(
            new ProductDTO("", productPrice, productCount, ""));

    log.info("pay/status  {}", priceInfo);
    return ResponseEntity.ok(priceInfo);
  }

  @GetMapping("/price/promo-code")
  @Hidden
  public ResponseEntity<Map<String, List<?>>> calculatePriceAfterUsePromoCode(
      @RequestParam("productPrice") String productPrice,
      @RequestParam("productCount") String productCount,
      @RequestParam("promoCode") String promoCode) {
    log.info(
        "Class: {}, Method: calculatePriceAfterUsePromoCode - get request about price Info.",
        className);
    Map<String, List<?>> priceInfo =
        paymentService.calculatePriceAfterUsePromoCode(
            new ProductDTO("", productPrice, productCount, ""), promoCode);

    log.info("pay/price  {}", priceInfo);
    return ResponseEntity.ok(priceInfo);
  }

  @Operation(
      summary = "Get information about the promo code",
      description = "Returns information about the specified promo code, if one exists.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Promo code processed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Promo code not found",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content =
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
      })
  @GetMapping("/promo-code")
  public ResponseEntity<AppResponse> getPromoCode(@RequestParam("promoCode") String promoCode) {
    log.info("Class: {}, Method: getPromoCode - get request about price Info.", className);
    PromoCode promoCodeInfo = paymentService.getPromoCode(promoCode);
    AppResponse response =
        new AppResponse(200, "The promo code has been processed successfully.", promoCodeInfo);
    log.info("Class: {}, Method: getPromoCode - return promo code info.", promoCodeInfo);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/funds/{userId}")
  @PreAuthorize("#userId == authentication.principal['id']")
  public ResponseEntity<AppResponse> createFundsRequest(
      @PathVariable("userId") String userId, @RequestBody FundsRequest fundsRequest) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("Class: {}, Method: {} - createFundsRequest.", className, methodName);
    String message = paymentService.saveFundsRequest(userId, fundsRequest);
    AppResponse response =
        new AppResponse(200, "The promo code has been processed successfully.", message);
    return ResponseEntity.ok(response);
  }

  //  @GetMapping("/order-status")
  //  public ResponseEntity<AppResponse> getOrderStatus(@RequestParam("orderReference") String
  // orderReference,
  //                                                  @RequestParam("merchantSignature") String
  // merchantSignature) {
  //    log.info("Class: {}, Method: getOrderStatus - get request about getOrderStatus Info.",
  // className);
  //    String url = "https://api.wayforpay.com/api";
  //    String param= "evently_book_vercel_app;"+orderReference;
  //    WayforpayRequest wayforpayRequest = new WayforpayRequest(
  //        "CHECK_STATUS",
  //        "evently_book_vercel_app",
  //        orderReference,
  //        generateSignature("07e12edf1d5f39eaf8b1b7fd029cd10f2b557c3e", param),
  //        1
  //    );
  //    HttpHeaders headers = new HttpHeaders();
  //    headers.setContentType(MediaType.APPLICATION_JSON);
  //    HttpEntity<WayforpayRequest> request = new HttpEntity<>(wayforpayRequest, headers);
  //
  ////    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
  //    ResponseEntity<Map> response = restTemplate.exchange(
  //        url,
  //        HttpMethod.POST,
  //        request,
  //        Map.class
  //    );
  //    AppResponse appresponse = new AppResponse(200,
  //        "The order status has been processed successfully.",
  //        response
  //    );
  //    log.info("Class: {}, Method: getPromoCode - return getOrderStatus info. {}", className,
  // response);
  //
  //    return ResponseEntity.ok(appresponse);
  //  }
}
