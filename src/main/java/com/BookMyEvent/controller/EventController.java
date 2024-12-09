package com.BookMyEvent.controller;

import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "Events Controller")
@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        log.info("Class: {}, Method: createEvent - Creating new event", this.getClass().getSimpleName());
        EventDTO createdEvent = eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable String id, @RequestBody EventDTO eventDTO) {
        log.info("Class: {}, Method: updateEvent - Updating event with id: {}", this.getClass().getSimpleName(), id);
        EventDTO updatedEvent = eventService.updateEvent(id, eventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable String id) {
        log.info("Class: {}, Method: deleteEvent - Deleting event with id: {}", this.getClass().getSimpleName(), id);
        eventService.deleteEvent(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Event deleted successfully");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @Operation(
        summary = "Get All Events",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Get All Events",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = Event.class))
                })
        })
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEventsUA() {
        log.info("Class: {}, Method: getAllEvents - Fetching all events.", this.getClass().getSimpleName());
        List<EventResponseDto> events = eventService.getEventsUA();
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/clearPastEvents")
    public ResponseEntity<Void> clearPastEvents() {
        log.info("Class: {}, Method: clearPastEvents - Clearing past events.", this.getClass().getSimpleName());
        eventService.deletePastEvents();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @Operation(
            summary = "Update Event Status",
            description = "Updates the status of the event to one of the predefined values: PENDING, APPROVED, or CANCELLED.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "The unique identifier of the event to update",
                            required = true,
                            example = "event123"
                    ),
                    @Parameter(
                            name = "status",
                            description = "The new status of the event",
                            required = true,
                            example = "APPROVED"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Event status updated successfully",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = EventDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Event not found",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<EventDTO> updateEventStatus(@PathVariable String id, @RequestParam EventStatus status) {
        log.info("Class: {}, Method: updateEventStatus - Updating status of event ID: {} to {}", this.getClass().getSimpleName(), id, status);
        EventDTO updatedEvent = eventService.updateEventStatus(id, status);
        return ResponseEntity.ok(updatedEvent);
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Event>> getEventsByStatus(@PathVariable("status") EventStatus status) {
        log.info("Class: {}, Method: getEventsByStatus - Fetching events with status: {}", this.getClass().getSimpleName(), status);
        List<Event> events = eventService.getEventsByStatus(status);
        if (events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(events);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<EventDTO> approveEvent(@PathVariable String id) {
        log.info("Class: {}, Method: approveEvent - Approving event with ID: {}", this.getClass().getSimpleName(), id);
        EventDTO approvedEvent = eventService.approveEvent(id);
        return ResponseEntity.ok(approvedEvent);
    }
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        log.info("Class: {}, Method: getAllEvents - Fetching all approved events.", this.getClass().getSimpleName());
        List<Event> events = eventService.getEventsByStatus(EventStatus.APPROVED);
        return ResponseEntity.ok(events);
    }
}