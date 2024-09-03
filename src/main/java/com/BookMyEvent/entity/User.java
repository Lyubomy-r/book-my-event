package com.BookMyEvent.entity;

import com.BookMyEvent.entity.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  public User(String name, String email, String password, boolean mailConfirmation, Role role, LocalDateTime creationDate, String location) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.mailConfirmation = mailConfirmation;
    this.role = role;
    this.creationDate = creationDate;
    this.location = location;
  }

  @Id
  private String id;
  private String name;
  @Indexed(unique = true)
  private String email;
  private String password;
  private boolean mailConfirmation;
  private Role role;
  private LocalDateTime creationDate;
  private String location;

}
