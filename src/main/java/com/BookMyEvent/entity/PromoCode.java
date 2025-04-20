package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "promo_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {
  @Id
  private ObjectId id;
  @Indexed(unique = true)
  private String name;
  private int percentage;
}
