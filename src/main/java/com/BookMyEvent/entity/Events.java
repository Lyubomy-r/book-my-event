package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Events {

  @Id
  private String id;
  private String title;
  private String description;
  private String photoUrl;
  private LocalDateTime eventStartDate;
  private LocalDateTime creationDate;
  private String phoneNumber;
  private Long ticketPrice;
  private String location;
  private List<Users> organizers;

}
