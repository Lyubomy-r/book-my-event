package com.BookMyEvent.controller;

import com.BookMyEvent.entity.PromoCode;
import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventFilterRequest;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.security.SecurityUser;
import com.BookMyEvent.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

import static com.BookMyEvent.config.SwaggerConfig.CREATED_EVENT_PAYLOAD_SCHEMA;
import static com.BookMyEvent.config.SwaggerConfig.PAGE_EVENT_RESPONSEDTO_PAYLOAD_SCHEMA;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "Events Controller")
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private String className = this.getClass().getSimpleName();

    @Operation(
        summary = "Create a new event",
        description = CREATED_EVENT_PAYLOAD_SCHEMA,
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Event created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventResponseDto.class))
            ),
            @ApiResponse(responseCode = "400",
                description = "Invalid request (e.g., validation errors, file size too large)",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponseDto.class)
                    )
                })
        }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponseDto> createEvent(
        @Parameter(description = "Event details, including the name, description, and date", required = true)
        @RequestPart("event") @Valid EventDTO eventDTO,
        @Parameter(description = "First event image (Max 1MB)", required = true)
        @RequestPart(value = "firstImage", required = true) MultipartFile firstImage,
        @Parameter(description = "Second event image (Max 1MB)", required = false)
        @RequestPart(value = "secondImage", required = false) MultipartFile secondImage,
        @Parameter(description = "Third event image (Max 1MB)", required = false)
        @RequestPart(value = "thirdImage", required = false) MultipartFile thirdImage) {
        log.info("Class: {}, Method: createEvent - firstImageBase64 {}", className, firstImage.getOriginalFilename());
        EventResponseDto createdEvent = eventService.createEvent(eventDTO, firstImage, secondImage, thirdImage);
        log.info("Class: {}, Method: createEvent - Creating new event", className);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @Operation(summary = "Update an event",
        description = "Updates an existing event with the given ID. Only the event organizer can update the event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event updated successfully",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "User is not the organizer of the event",
            content = {
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)
                )}),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = {
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)
                )}),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = {
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)
                )})
    })
    @PutMapping("/{id}")
    public ResponseEntity<AppResponse> updateEvent(@PathVariable String id,
                                                   @RequestBody EventDTO eventDTO,
                                                   @AuthenticationPrincipal Map<String, Object> principal,
                                                   @Parameter(description = "Second event image (Max 1MB)", required = false)
                                                   @RequestPart(value = "secondImage", required = false) MultipartFile secondImage,
                                                   @Parameter(description = "Third event image (Max 1MB)", required = false)
                                                   @RequestPart(value = "thirdImage", required = false) MultipartFile thirdImage) {
        String userId = (String) principal.get("id");
        log.info("Class: {}, Method: updateEvent - Updating event with id: {}  authentication user {}", className, id, userId);
        AppResponse response = new AppResponse(200,
            eventService.makeUpdateEventRequest(id, eventDTO, userId, secondImage, thirdImage)
        );
        log.info("Class: {}, Method: updateEvent - return successfully code message", className);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update event image",
        description = "Updates the image for an event by its ID. Maximum file size: 1MB."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event image updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = EventResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file format or file size exceeded",
            content = @Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            )),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            ))
    })
    @PatchMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponseDto> updateEventImage(
        @Parameter(description = "Event ID", required = true, example = "60d21b4667d0d8992e610c85")
        @PathVariable String id,
        @Parameter(description = "User Avatar  image (Max 1MB)", required = true)
        @RequestPart(value = "eventImage", required = true) MultipartFile eventImage) {
        log.info("Class: {}, Method: updateEventImage - Updating Img event with id: {}", className, id);
        EventResponseDto updatedEvent = eventService.updateEventImage(id, eventImage);
        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(
        summary = "Delete an event",
        description = "Deletes an event by its ID. If the event does not exist, a 404 error is returned."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event deleted successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AppResponse.class))),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            ))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponse> deleteEvent(
        @Parameter(description = "Event ID", required = true, example = "60d21b4667d0d8992e610c85")
        @PathVariable String id) {
        eventService.deleteEvent(id);
        AppResponse response = new AppResponse(
            HttpStatus.OK.value(), "Event deleted successfully");
        log.info("Class: {}, Method: deleteEvent - Deleting event with id: {}", className, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
                        schema = @Schema(implementation = Page.class))
                })
        })
    @GetMapping
    public ResponseEntity<Page<EventResponseDto>> getAllApprovedEvents(@PageableDefault(
        page = 0,
        size = 6) Pageable pageable) {
        log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", className);
        Page<EventResponseDto> events = eventService.getApprovedEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(
        summary = "Get filtered approved events",
        description = "Retrieves a paginated list of approved events based on various filters. "+
            "Example request: /api/v1/events/filtered?page=0&size=6"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of filtered approved events",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class),
            examples = @ExampleObject(value = PAGE_EVENT_RESPONSEDTO_PAYLOAD_SCHEMA))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
            content = @Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDto.class)
            ))
    })
    @PostMapping("/filtered")
    public ResponseEntity<Page<EventResponseDto>> getAllFilteredApprovedEvents(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
                Filtering criteria for events. Field eventTypes has allowable values {"STAND_UP", "UNDER_HOUSE", "CONCERTS",
                   "MASTER_CLASS", "BUSINESS_NETWORKING", "SPORTS_EVENTS", "OTHER"
              }""",
            required = true,
            content = @Content(
                schema = @Schema(implementation = EventFilterRequest.class)
            )
        )
        @RequestBody EventFilterRequest eventFilterRequest,
        @Parameter(description = "Pagination information (page, size). Example: /filtered?page=0&size=6")
        @PageableDefault(
            page = 0,
            size = 6) Pageable pageable) {
        log.info("Class: {}, Method: getAllFilteredApprovedEvents - Fetching all APPROVED events. param {}", className, eventFilterRequest);
        Page<EventResponseDto> events = eventService.filterEvents(eventFilterRequest, pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(
        summary = "Get Approved Event by eventId",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Get info by Approved Event.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = EventResponseDto.class))
                }),
            @ApiResponse(
                responseCode = "404",
                description = "Event with the provided id not found or id is from not approved event.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponseDto.class)
                    )
                })
        })
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable("eventId") String eventId) {
        log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", className);
        EventResponseDto events = eventService.getApprovedEventById(eventId);
        return ResponseEntity.ok(events);
    }

    @Operation(
        summary = "Get USER Created Events by user id.",
        description = "Get USER Created Events info by user id.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = " Return USER list of created events.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = UserResponseDto.class)
                    )}
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request or not Validation failed. User ID cannot be null or empty",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponseDto.class))
                }),
            @ApiResponse(
                responseCode = "404",
                description = "User with ID not found.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponseDto.class)
                    )
                }),
            @ApiResponse(
                responseCode = "403",
                description = "Access forbidden. User ID does not match token ID",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(
                        example = "{\n" +
                            "    \"timestamp\": \"2024-12-19T16:40:54.575+00:00\",\n" +
                            "    \"status\": 403,\n" +
                            "    \"error\": \"Forbidden\",\n" +
                            "    \"path\": \"/api/v1/users/674cb373e84f0654529647c4\"\n" +
                            "}"
                    )
                )
            )
        })
    @GetMapping("/user/{userId}")
//    @PreAuthorize("#userId == authentication.principal['id']")
    public ResponseEntity<Page<EventResponseDto>> getByOrganizersId(@PathVariable("userId") String userId,
                                                                    @PageableDefault(
                                                                        page = 0,
                                                                        size = 6) Pageable pageable) {
        Page<EventResponseDto> userResponse = eventService.getByOrganizersId(userId, pageable);
        log.info("UserController::findUserCreatedEvents - /users/{userId} - Return User Info id: {} .", userId);
        return ResponseEntity.ok(userResponse);
    }

//    @Hidden
//    @DeleteMapping("/clearPastEvents")
//    public ResponseEntity<AppResponse> clearPastEvents() {
//        log.info("Class: {}, Method: clearPastEvents - Clearing past events.", className);
//        eventService.deletePastEvents();
//        AppResponse response = new AppResponse(
//            HttpStatus.OK.value(), "All Event deleted successfully");
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Hidden
//    @PostMapping("/url/imag")
//    public ResponseEntity<AppResponse> clearPastEvents2() {
//        log.info("Class: {}, Method: /url/imag - Clearing past events.", className);
//        eventService.chdb();
//        AppResponse response = new AppResponse(
//            HttpStatus.OK.value(), "All Event /url/imag successfully");
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @Hidden
//    @PostMapping("/location/ch")
//    public ResponseEntity<AppResponse> clearlocation() {
//        log.info("Class: {}, Method: /location/ch - Clearing location.", className);
//        eventService.chdbConrdinatis();
//        AppResponse response = new AppResponse(
//            HttpStatus.OK.value(), "All Event location successfully");
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
}