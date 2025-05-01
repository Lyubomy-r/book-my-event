package com.BookMyEvent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WayforpayRequest {
  private String transactionType;
  private String merchantAccount;
  private String orderReference;
  private String merchantSignature;
  private Integer apiVersion;

}

