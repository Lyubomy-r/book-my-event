package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.Location;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TicketDto(
    String id,
    String title,
    @JsonProperty("price")
    String ticketPrice,
    String ticketReference,
    DateDetails date,
    String row,
    String seat,
    String status,
    Location location,
    List<Image> images,
    String eventUrl,
    String eventFormat,
    String orderDate,
    String orderDetailsId,
    String eventId,
    String userId) {}
