package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private String id;
    private String name;
    private String email;
    private LocalDateTime creationDate;
    private String location;
    private Status status;
}
