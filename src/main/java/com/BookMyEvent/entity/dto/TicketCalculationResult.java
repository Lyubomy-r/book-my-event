package com.BookMyEvent.entity.dto;

public record TicketCalculationResult(
        Integer productCount,
        Integer productPrice,
        Double productAmount,
        Integer soldTickets,
        Integer availableTickets
) {}