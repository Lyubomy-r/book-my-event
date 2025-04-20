package com.BookMyEvent.entity;

import com.BookMyEvent.entity.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetails {
  String merchantAccount;
  String merchantAuthType;
  String merchantDomainName;
  //                             String amount,
  String currency;
  String orderTimeout;
  String holdTimeout;
  ProductDTO product;
  String clientFirstName;
  String clientLastName;
  String clientEmail;
  String clientPhone;
  String defaultPaymentSystem;
  String serviceUrl;
  String merchantSignature;
  String authCode;
  String processingDate;
  String cardPan;
  String issuerBankCountry;
  String issuerBankName;
  String recToken;
  String transactionStatus;
  String reason;
  String reasonCode;
  String fee;
  String paymentSystem;
}
