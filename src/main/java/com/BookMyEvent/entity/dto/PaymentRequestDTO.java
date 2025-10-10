package com.BookMyEvent.entity.dto;

import jakarta.validation.Valid;

import java.util.List;

public record PaymentRequestDTO(
                                String userId,
                                @Valid ProductDTO product,
                                String clientFirstName,
                                String clientLastName,
                                String clientPhone,
                                String clientEmail
                                ) {
}
