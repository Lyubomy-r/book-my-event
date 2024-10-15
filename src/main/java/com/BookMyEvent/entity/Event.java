package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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
  private LocalDateTime eventStartDate;
  private LocalDateTime creationDate;
  private Integer availableTickets;
  private String phoneNumber;
  private Long ticketPrice;
  private Integer numberOfTickets;
  private String location;
  private List<User> organizers;
  private double rating;
  private EventType eventType;

}
