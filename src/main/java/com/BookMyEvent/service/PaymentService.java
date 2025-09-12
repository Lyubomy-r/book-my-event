package com.BookMyEvent.service;

import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.PromoCode;
import com.BookMyEvent.entity.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentService {
  PaymentResponseDTO prepareForPayment(String eventId, PaymentRequestDTO paymentRequest);

  Map<String, String> paymentVerification(PaymentStatusResponseDTO statusResponse);

  String paymentVerificationFreeEvents(String eventId, PaymentRequestDTO paymentRequest);

  OrderDetails findByOrderReference(String orderReference);

  Map<String, List<?>> calculatePriceAfterUsePromoCode(ProductDTO product, String promoCode);

  Map<String, List<?>> calculatePriceBeforeUsePromoCode(ProductDTO product);

  PromoCode getPromoCode(String promoCode);

   String saveFundsRequest(String userId, CreateFundsRequestDTO fundsRequest);

  BigDecimal getOrganizerWithdrawnFunds(String userId);

  BigDecimal getOrganizerFunds(String userId);
}
