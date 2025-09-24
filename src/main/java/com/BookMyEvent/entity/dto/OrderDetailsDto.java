package com.BookMyEvent.entity.dto;

public record OrderDetailsDto(String id,
                              String orderReference,
                              String orderDate,
                              String productCount,
                              String amount,
                              EventResponseDto event,
                              UserResponseDto user,
                              String status){}
