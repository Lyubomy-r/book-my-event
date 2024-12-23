package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthdayDate;
    private LocalDateTime creationDate;
    private boolean mailConfirmation;
    private Role role;
    private String location;
    private String avatarUrl;
    private String phoneNumber;
    private Status status;
}
