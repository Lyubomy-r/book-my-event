package com.BookMyEvent.entity.dto;

import java.util.List;

public record PaymentRequestDTO(
                                String userId,
                                List<ProductDTO> product,
                                String clientFirstName,
                                String clientLastName,
                                String clientPhone,
                                String clientEmail
                                ) {
}
