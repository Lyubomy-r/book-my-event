package com.BookMyEvent.entity;

import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "event_update_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUpdateRequest {

  @Id
  private String id;
  private String eventId;
  private String title;
  private String description;
  @JsonProperty("price")
  private Long ticketPrice;
//  private Integer availableTickets;
  private Boolean unlimitedTickets;
  private Integer numberOfTickets;
  private String aboutOrganizer;
  @JsonProperty("type")
  private EventType eventType;
  @JsonProperty("category")
  private EventCategory eventCategory;
  private EventStatus eventStatus = EventStatus.PENDING_UPDATE_REQUEST;
  @DBRef
  private List<Image> images=new ArrayList<>();

  public void linkImageWithEvent(Image image) {
    this.getImages().add(image);
  }

  public void linkAllImageWithEvent(List<Image> image) {
    this.getImages().addAll(image);
  }
}
