package com.BookMyEvent.entity.dto;

import java.util.List;

public record PaymentResponseDTO(String merchantAccount,
                                 String merchantAuthType,
                                 String merchantDomainName,
                                 String orderReference,
                                 String orderDate,
                                 String amount,
                                 String currency,
                                 String orderTimeout,
                                 String holdTimeout,
                                 List<ProductDTO> product,
                                 String clientFirstName,
                                 String clientLastName,
                                 String clientAddress,
                                 String clientCity,
                                 String clientEmail,
                                 String defaultPaymentSystem,
                                 String serviceUrl,
                                 String merchantSignature
) {
}
