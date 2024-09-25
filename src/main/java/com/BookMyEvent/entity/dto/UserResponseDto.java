package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
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
    private boolean mailConfirmation;
    private Role role;
    private String location;
    private Status status;
}
