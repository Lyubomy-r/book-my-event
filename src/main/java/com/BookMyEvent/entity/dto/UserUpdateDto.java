package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

  private String id;
  private String name;
  private String email;
  private String location;
  private Status status;
}
