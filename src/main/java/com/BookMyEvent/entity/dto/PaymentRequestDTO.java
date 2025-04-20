package com.BookMyEvent.entity.dto;

import java.util.List;

public record PaymentRequestDTO(
                                String userId,
                                ProductDTO product,
                                String clientFirstName,
                                String clientLastName,
                                String clientPhone,
                                String clientEmail
                                ) {
}
