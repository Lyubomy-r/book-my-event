package com.BookMyEvent.entity;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  private ObjectId id;
  private String name;
  @Indexed(unique = true)
  private String email;
  private String password;
  private boolean mailConfirmation;
  private Role role;
  private LocalDateTime creationDate;
  private Long phone;
  private String location;
  private Status status;


  @DBRef
  private List<Event> createdEvents = new ArrayList<>();
}
