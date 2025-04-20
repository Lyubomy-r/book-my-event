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
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetails {
  @Id
  private ObjectId id;
  private String orderReference;
  private LocalDateTime reservationExpires;
  private Instant orderDate;
  private Long row;
  private Long seat;
  private PaymentDetails paymentDetails;
  @DBRef
  private Event event;
  @DBRef
  private User user;
  private OrderStatus status;
}
