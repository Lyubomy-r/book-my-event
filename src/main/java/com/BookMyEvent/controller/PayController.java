package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;
import com.BookMyEvent.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
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
}
