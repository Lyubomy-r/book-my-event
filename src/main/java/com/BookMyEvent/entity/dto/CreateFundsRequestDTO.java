package com.BookMyEvent.entity.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateFundsRequestDTO {
    private String cartNumber;
    private BigDecimal amount;
}
