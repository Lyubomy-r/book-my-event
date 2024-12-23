package com.BookMyEvent.controller;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.service.EventService;
import com.BookMyEvent.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventDTO> createEvent(
        @RequestPart("event") @Valid EventDTO eventDTO,
        @RequestPart("firstImage") MultipartFile firstImage,
        @RequestPart("secondImage") MultipartFile secondImage,
        @RequestPart("thirdImage") MultipartFile thirdImage) {
        log.info("Class: {}, Method: createEvent - Creating new event", this.getClass().getSimpleName());
        EventDTO createdEvent = eventService.createEvent(eventDTO, firstImage, secondImage, thirdImage);
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
        summary = "Get All APPROVED Events",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Get All APPROVED Events.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = Event.class))
                })
        })
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllApprovedEvents() {
        log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", this.getClass().getSimpleName());
        List<EventResponseDto> events = eventService.getEventsUA();
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/clearPastEvents")
    public ResponseEntity<Void> clearPastEvents() {
        log.info("Class: {}, Method: clearPastEvents - Clearing past events.", this.getClass().getSimpleName());
        eventService.deletePastEvents();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}