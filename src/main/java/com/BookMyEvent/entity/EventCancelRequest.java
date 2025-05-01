package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "event_cancel_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCancelRequest {
  @Id
  private ObjectId id;
  private String eventId;
  private String userId;
  private String contact;
  private String reason;
}
