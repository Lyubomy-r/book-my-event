package com.BookMyEvent.entity;

import com.BookMyEvent.entity.Enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "tickets")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ticket {
  @Id
  private String id;
  private String title;
  private String ticketPrice;
  private String ticketReference;
  private DateDetails date;
//  private String description;
//  private ObjectId eventId;
//  private ObjectId userId;
//  private LocalDate startDate;
//  private LocalDateTime reservationExpires;
//  private LocalTime startTime;
  private Long row;
  private Long seat;
//  private int numberOfTickets;
  private OrderStatus status;
  private Location location;
  @DBRef
  private List<Image> images;
  private String eventUrl;
  private String eventFormat;
  private Instant orderDate;
  private String orderDetailsId;
  private String eventId;
  private String userId;
  private String productCount;
  private String amount;

}
