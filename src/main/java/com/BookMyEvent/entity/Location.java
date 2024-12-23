package com.BookMyEvent.entity;

import jakarta.validation.constraints.NotBlank;

public record Location(@NotBlank String city,   @NotBlank String street, String venue, String latitude, String longitude) {
}
