package com.BookMyEvent.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStatusResponseDTO{
  private String merchantAccount;
  private String orderReference;
  private String merchantSignature;
  private String amount;
  private String currency;
  private String authCode;
  private String email;
  private String phone;
  private String createdDate;
  private String processingDate;
  private String cardPan;
  private String cardType;
  private String issuerBankCountry;
  private String issuerBankName;
  private String recToken;
  private String transactionStatus;
  private String reason;
  private String reasonCode;
  private String fee;
  private String paymentSystem;
}
