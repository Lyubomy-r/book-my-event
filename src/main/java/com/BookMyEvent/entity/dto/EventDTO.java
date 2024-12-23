package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {
    private String id;
    @NotBlank(message = "Title is mandatory")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    @NotBlank(message = "Description is mandatory")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    private String photoUrl;

//    @NotNull(message = "Event start date is mandatory")
//    @Future(message = "Event start date must be in the future")
//    private LocalDateTime eventStartDate;
    private DateDetails dateDetails;
    private LocalDateTime creationDate;
    private String phoneNumber;
    @NotNull(message = "Number of tickets is mandatory")
    @Min(value = 0, message = "Ticket price must be a positive number")
    private Long ticketPrice;
    @NotNull(message = "Number of tickets is mandatory")
    @Min(value = 1, message = "Number of tickets must be at least 1")
    private Integer numberOfTickets;
    private Integer availableTickets;
    @Valid
    @NotNull
    private Location location;
    private List<User> organizers;
    private boolean isDeleted = false;
    private String eventUrl;
    private EventStatus eventStatus;
}
