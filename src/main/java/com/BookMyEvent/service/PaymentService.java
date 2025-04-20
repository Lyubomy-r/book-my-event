package com.BookMyEvent.service;

import com.BookMyEvent.entity.PromoCode;
import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;
import com.BookMyEvent.entity.dto.PaymentStatusResponseDTO;
import com.BookMyEvent.entity.dto.ProductDTO;

import java.util.List;
import java.util.Map;

public interface PaymentService {
  PaymentResponseDTO prepareForPayment(String eventId, PaymentRequestDTO paymentRequest);

  void paymentVerification(PaymentStatusResponseDTO statusResponse);

  Map<String, List<?>> calculatePriceAfterUsePromoCode(ProductDTO product, String promoCode);

  Map<String, List<?>> calculatePriceBeforeUsePromoCode(ProductDTO product);

  PromoCode getPromoCode(String promoCode);
}
