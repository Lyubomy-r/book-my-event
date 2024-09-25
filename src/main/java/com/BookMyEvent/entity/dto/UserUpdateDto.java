package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.Enums.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

  private String id;

  @Pattern(regexp = "^[\\p{L} ]{3,50}", message = "Write a correct First Name. Use only chars. Min 3 not more than 30.")
  private String name;

  @Email(message = "Incorrect email format")
  private String email;
  private String location;
  private Status status;
}
