package com.BookMyEvent.entity.dto;

public record PaymentStatusResponseDTO(String merchantAccount,
                                       String orderReference,
                                       String merchantSignature,
                                       String amount,
                                       String currency,
                                       String authCode,
                                       String email,
                                       String phone,
                                       String createdDate,
                                       String processingDate,
                                       String cardPan,
                                       String issuerBankCountry,
                                       String issuerBankName,
                                       String recToken,
                                       String transactionStatus,
                                       String reason,
                                       String reasonCode,
                                       String fee,
                                       String paymentSystem
                                       ) {
}
