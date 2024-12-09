package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {

  private String id;
  private String title;
  private String description;
  private String photoUrl;
  @JsonProperty("date")
  private DateDetails date;
  private LocalDateTime creationDate;
  @JsonProperty("tickets")
  private Integer availableTickets;
  private String phoneNumber;
  @JsonProperty("price")
  private Long ticketPrice;
  private Integer numberOfTickets;
  private Location location;
  private List<User> organizers;
  private double rating;
  @JsonProperty("type")
  private String eventType;
  @JsonProperty("category")
  private String eventCategory;
  private String eventUrl;
  private EventStatus eventStatus;
}
