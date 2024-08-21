package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {

  @Id
  private String id;
  private String name;
  @Indexed(unique = true)
  private String email;
  private String password;
  private LocalDateTime creationDate;
  private String location;

}
