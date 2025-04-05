package com.BookMyEvent.service;

import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;

public interface PaymentService {
  PaymentResponseDTO prepareForPayment(String eventId, PaymentRequestDTO paymentRequest);
}
