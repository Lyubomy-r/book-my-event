package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Image;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EventUpdateRequestDTO(String id,
                                    String eventId,
                                    String title,
                                    String description,
                                    @JsonProperty("price")
                                    Long ticketPrice,
                                    Boolean unlimitedTickets,
                                    Integer numberOfTickets,
                                    String aboutOrganizer,
                                    @JsonProperty("type")
                                    String eventType,
                                    @JsonProperty("category")
                                    EventCategory eventCategory,
                                    EventStatus eventStatus,
                                    List<Image> images) {
}
