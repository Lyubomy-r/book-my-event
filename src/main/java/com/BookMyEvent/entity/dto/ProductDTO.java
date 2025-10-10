package com.BookMyEvent.entity.dto;

import jakarta.validation.constraints.Pattern;

public record ProductDTO(String productName,
                         @Pattern(
                                 regexp = "^[0-9]+$",
                                 message = "Ціна повинна містити лише цифри"
                         )
                         String productPrice,
                         @Pattern(
                                 regexp = "^[0-9]+$",
                                 message = "Кількість повинна містити лише цифри"
                         )
                         String productCount,
                         @Pattern(
                                 regexp = "^[0-9]+([.][0-9]{1,2})?$",
                                 message = "Сума повинна бути числом з не більше ніж двома знаками після крапки."
                         )
                         String amount) {
}
