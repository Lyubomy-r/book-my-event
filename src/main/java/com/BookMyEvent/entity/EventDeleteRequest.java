package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "event_cancel_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDeleteRequest {
  @Id
  private String id;
  private String eventId;
  private String userId;
  private String contact;
  private String reason;

  public EventDeleteRequest(String eventId,
                            String userId,
                            String contact,
                            String reason) {
    this.eventId = eventId;
    this.userId = userId;
    this.contact = contact;
    this.reason = reason;
  }
}
