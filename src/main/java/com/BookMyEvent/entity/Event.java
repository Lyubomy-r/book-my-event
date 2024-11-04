package com.BookMyEvent.entity;

import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

  @Id
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
  @JsonIgnore
  private Integer numberOfTickets;
  private Location location;
  private List<User> organizers;
  private double rating;
  @JsonProperty("type")
  private EventType eventType;
  @JsonProperty("category")
  private EventCategory eventCategory;

  @DBRef
  private User createdBy;

  
}
