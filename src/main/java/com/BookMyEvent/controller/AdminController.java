package com.BookMyEvent.controller;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.EventDeleteRequest;
import com.BookMyEvent.entity.EventUpdateRequest;
import com.BookMyEvent.entity.dto.*;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.EventService;
import com.BookMyEvent.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.Map;

import static com.BookMyEvent.config.SwaggerConfig.PAGE_EVENT_RESPONSEDTO_PAYLOAD_SCHEMA;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Tag(name = "Admin Endpoints")
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

  private final UserService userService;
  private final EventService eventService;

  private String className = this.getClass().getSimpleName();

  @Operation(
      summary = "Get  list of users",
      description = "Retrieves a list of users.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = " List of users successfully retrieved",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      array = @ArraySchema(schema = @Schema( implementation = UserResponseDto.class))
                  )}
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid page or size parameter",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponseDto.class)
                  )
              })
      })
  @GetMapping("/users")
  public ResponseEntity<Page<UserResponseDto>> findAllUserProfiles(@PageableDefault(
      page = 0,
      size = 8) Pageable pageable) {
    Page<UserResponseDto> userList = userService.findAllUserProfiles(pageable);
    log.info("{}::findAllUsers - /admin/users - Return list of user.", this.getClass().getSimpleName());
    return ResponseEntity.ok(userList);
  }

  @Operation(
      summary = "Get USER info by id.",
      description = "Get USER info by id.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = " Return USER info.",
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
              description = "Access forbidden. Can use Admin.",
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
  @GetMapping("/users/{userId}")
  public ResponseEntity<UserResponseDto> findUserInfoById(@PathVariable("userId") String userId) {
    UserResponseDto userResponse = userService.findUserInfoById(userId);
    log.info("UserController::findUserInfoById - /users/{userId} - Return User Info email: {} .", userResponse.getEmail());
    return ResponseEntity.ok(userResponse);
  }

  @Operation(
      summary = "Get All Events",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Get All Events to admin. Sorted by TOP_EVENTS and CreationDate.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = Event.class))
              })
      })
  @GetMapping("/events")
  public ResponseEntity<Page<EventResponseDto>> getAllEvents(@PageableDefault(
      page = 0,
      size = 8,
  sort = "creationDate",
      direction = Sort.Direction.DESC) Pageable pageable) {
    log.info("Class: {}, Method: getAllEvents - Fetching all events.", this.getClass().getSimpleName());
    Page<EventResponseDto> events = eventService.getAllEvents(pageable);
    return ResponseEntity.ok(events);
  }

  @Operation(
      summary = "Get Event by eventId",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Get info by Event.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = EventResponseDto.class))
              }),
          @ApiResponse(
              responseCode = "404",
              description = "Event with the provided id not found",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponseDto.class)
                  )
              })
      })
  @GetMapping("/events/{eventId}")
  public ResponseEntity<EventResponseDto> getEventById(@PathVariable("eventId") String eventId) {
    log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", className);
    EventResponseDto events = eventService.getEventById(eventId);
    return ResponseEntity.ok(events);
  }

  @Operation(
      summary = "Get Event Update Request by eventId",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Get info by Event Update.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = EventResponseDto.class))
              }),
          @ApiResponse(
              responseCode = "404",
              description = "Event with the provided id not found",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponseDto.class)
                  )
              })
      })
  @GetMapping("/events/update/request/{eventId}")
  public ResponseEntity<EventUpdateRequestDTO> getEventUpdateRequest(@PathVariable("eventId") String eventId) {
    log.info("Class: {}, Method: getEventUpdateRequest - Fetching event update request.", className);
      EventUpdateRequestDTO events = eventService.getEventUpdateRequestById(eventId);
    log.info("Class: {}, Method: getEventUpdateRequest - Fetching event update request.", className);
    return ResponseEntity.ok(events);
  }

  @Operation(
      summary = "Get Event canceled Request by eventId",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Get info by Event canceled info.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = EventResponseDto.class))
              }),
          @ApiResponse(
              responseCode = "404",
              description = "Event with the provided id not found",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponseDto.class)
                  )
              })
      })
  @GetMapping("/events/cancel/request/{eventId}")
  public ResponseEntity<EventDeleteRequest> getEventCanceledRequest(@PathVariable("eventId") String eventId) {
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    log.info("Class: {}, Method: {} - Fetching event canceled request.", className, methodName);
    EventDeleteRequest events = eventService.getEventCancelRequestById(eventId);
    log.info("Class: {}, Method: {} - Fetching event canceled request.", className, methodName);
    return ResponseEntity.ok(events);
  }

  @PatchMapping("/{id}/approve")
  public ResponseEntity<EventResponseDto> approveEvent(@PathVariable String id) {
    log.info("Class: {}, Method: approveEvent - Approving event with ID: {}", this.getClass().getSimpleName(), id);
    EventResponseDto approvedEvent = eventService.approveEvent(id);
    return ResponseEntity.ok(approvedEvent);
  }

  @Operation(
      summary = "Get USER Created Events by user id.",
      description = "Get USER Created Events info by user id. "+
          "Example request: /api/v1/admin/users/67ab5fbff1f4cb4bb7825f11/events?page=0&size=6",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = " Return list of events created by user.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(example = PAGE_EVENT_RESPONSEDTO_PAYLOAD_SCHEMA
                      )
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
                          "    \"path\": \"string\"\n" +
                          "}"
                  )
              )
          )
      })
  @GetMapping("/users/{userId}/events")
  public ResponseEntity<Page<EventResponseDto>> findUserCreatedEvents(
      @PathVariable("userId") String userId,
      @Parameter(name = "pageable", description = "Parameters for pagination (page, size). Example: /users/{userId}/events?page=0&size=6")
      @PageableDefault(
          page = 0,
          size = 6) Pageable pageable) {
    Page<EventResponseDto> userResponse = eventService.getByOrganizersId(userId, pageable);
    log.info("UserController::findUserCreatedEvents - /users/{userId} - Return User Info id: {} .", userId);
    return ResponseEntity.ok(userResponse);
  }

  @Operation(
      summary = "Delete a user by ID",
      description = "Deletes a user from the system using their unique user ID. If the user ID is not provided, invalid, or not found, an error is returned.",
      parameters = {
          @Parameter(
              name = "userId",
              description = "The unique identifier of the user to be deleted",
              required = true,
              example = "1229316345"
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "User deleted successfully",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = AppResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid user ID or missing ID",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "User not found",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          )
      }
  )
  @DeleteMapping("/users/{userId}")
  public ResponseEntity<AppResponse> deleteUser(@PathVariable("userId") String userId) {
    AppResponse response = new AppResponse(
        HttpStatus.OK.value(), userService.deleteFromAdmin(userId));
    log.info("{}::delete - /users/{userId} - Return deletion message.", this.getClass().getSimpleName());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Ban a user",
      description = "Sets the status of a specified user to 'BANNED', restricting their access to the system. If the user is already banned, an error is returned.",
      parameters = {
          @Parameter(
              name = "email",
              description = "Email of the user to ban",
              required = true,
              example = "user@example.com"
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "User status updated to 'BANNED'",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = AppResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "User already banned or not found. User with the specified email not found",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          ),
      }
  )
  @PatchMapping("/users/ban/{email}")
  public ResponseEntity<AppResponse> banUser(@PathVariable("email") String email) {
    AppResponse response = new AppResponse(HttpStatus.OK.value(), userService.banned(email));
    log.info("{}::banUser - /users/ban/{email} - Return ban message.", this.getClass().getSimpleName());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @Operation(
      summary = "Unban a user",
      description = "Sets the status of a specified user to 'ACTIVE', allowing them access to the system again. If the user is already active, no change is made.",
      parameters = {
          @Parameter(
              name = "email",
              description = "Email of the user to unban",
              required = true,
              example = "example@example.com"
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "User successfully unbanned or already active",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = AppResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "User not found with the specified email",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          )
      }
  )
  @PatchMapping("/users/unban/{email}")
  public ResponseEntity<AppResponse> unbanUser(@PathVariable("email") String email) {
    AppResponse response = new AppResponse(HttpStatus.OK.value(), userService.unbanned(email));
    log.info("{}::unbanUser - /users/unban/{email} - Returned unban user message.", this.getClass().getSimpleName());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @Operation(
      summary = "Get event count by status",
      description = "Returns a map where the key is the event status and the value is the number of events with that status.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Successful response",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(
                      type = "object"
                  ),
                  examples = @ExampleObject(
                      value = "{ \"CANCELLED\": 6, \"PENDING\": 9, \"APPROVED\": 34 }"
                  )
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Access forbidden.",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(
                      example = "{\n" +
                          "    \"timestamp\": \"2024-12-19T16:40:54.575+00:00\",\n" +
                          "    \"status\": 403,\n" +
                          "    \"error\": \"Forbidden\",\n" +
                          "    \"path\": \"string\"\n" +
                          "}"
                  )
              )
          )
      }
  )
  @GetMapping("/events/count/status")
  public ResponseEntity<Map<String, Integer>> getCountByStatus() {
    log.info("Class: {}, Method: getCountByStatus - Counting events for all statuses", this.getClass().getSimpleName());

    Map<String, Integer> statusCountMap = eventService.countByStatus();

    log.info("Events count by status: {}", statusCountMap);
    return ResponseEntity.ok(statusCountMap);
  }

  @Operation(
      summary = "Get events by status",
      description = "Fetches a paginated list of events based on their status. " +
          "Example request: /api/v1/admin/events/status/CANCELLED?page=0&size=6"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "List of events with the specified status",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = Page.class),
              examples = @ExampleObject(value = PAGE_EVENT_RESPONSEDTO_PAYLOAD_SCHEMA))),
      @ApiResponse(responseCode = "400", description = "Invalid status value or such status doesn't exist",
          content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content)
  })
  @GetMapping("/events/status/{status}")
  public ResponseEntity<Page<EventResponseDto>> getEventsByStatus(
      @Parameter(name = "status", description = "Events status [PENDING, APPROVED, CANCELLED]", example = "PENDING")
      @PathVariable("status") String status,
      @Parameter(name = "pageable", description = "Parameters for pagination (page, size). Example: /CANCELLED?page=0&size=6")
      @PageableDefault(
          page = 0,
          size = 8,
          sort = "creationDate",
          direction = Sort.Direction.DESC) Pageable pageable) {
    log.info("Class: {}, Method: getEventsByStatus - Fetching events with status: {}", this.getClass().getSimpleName(), status);
    Page<EventResponseDto> events = eventService.getEventsByStatus(status.toUpperCase(), pageable);

    return ResponseEntity.ok(events);
  }

  @Operation(
      summary = "Update Event Status",
      description = "Updates the status of the event to one of the predefined values: PENDING, APPROVED, or CANCELLED.",
      parameters = {
          @Parameter(
              name = "eventsId",
              description = "The unique identifier of the event to update",
              required = true,
              example = "event123"
          ),
          @Parameter(
              name = "status",
              description = "The new status of the event",
              required = true,
              example = "APPROVED"
          ),
          @Parameter(
              name = "urlToEvent",
              description = "Url link to the event",
              required = true,
              example = "string"
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
          ),
          @ApiResponse(
              responseCode = "409",
              description = "Event has update or cancel requests.",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          )
      }
  )
  @PatchMapping("/events/{eventsId}/status")
  public ResponseEntity<EventResponseDto> updateEventStatus(@PathVariable String eventsId,
                                                            @RequestParam String status,
                                                            @RequestParam String urlToEvent) {
    log.info("Class: {}, Method: updateEventStatus - Updating status of event ID: {} to {}", this.getClass().getSimpleName(), eventsId, status);
    EventResponseDto updatedEvent = eventService.updateEventStatus(eventsId, status, urlToEvent);
    log.info("Class: {}, Method: updateEventStatus - Event status updated successfully ID: {}", this.getClass().getSimpleName(), eventsId);
    return ResponseEntity.ok(updatedEvent);
  }

  @Operation(summary = "Update an event",
      description = "Updates an existing event with the given ID. Only the event organizer can update the event.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Event updated successfully",
          content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventResponseDto.class))),
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
  @PutMapping("/events/{eventsId}")
  public ResponseEntity<EventResponseDto> updateEvent(@PathVariable String eventsId,
                                                 @RequestParam("updateRequestId") String updateRequestId) {
    log.info("Class: {}, Method: updateEvent - Updating event with id: {} and updateRequestId: {}", className, eventsId, updateRequestId );
    EventResponseDto response = eventService.updateEvent(eventsId, updateRequestId);

    log.info("Class: {}, Method: updateEvent - return successfully code message", className);
    return ResponseEntity.ok(response);
  }

    @Operation(summary = "Cancel event update request",
            description = "Cancel event update request an existing event with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancel event update request successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventResponseDto.class))),
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
    @DeleteMapping("/events/{eventsId}/update/{updateRequestId}")
    public ResponseEntity<AppResponse> cancelEventUpdateRequest(@PathVariable("eventsId") String eventsId,
                                                        @PathVariable("updateRequestId") String updateRequestId) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("Class: {}, Method: {} - Updating event with id: {} and updateRequestId: {}", className, methodName, eventsId, updateRequestId );
        AppResponse response = new AppResponse(
                HttpStatus.OK.value(), eventService.cancelEventUpdateRequest(eventsId, updateRequestId));
        log.info("Class: {}, Method: {} - return successfully code message", className, methodName);
        return ResponseEntity.ok(response);
    }


  //  @Operation(
//      summary = "Delete not linked images",
//      description = "Delete not linked images"
//  )
  @Hidden
  @DeleteMapping("/img/img")
  public ResponseEntity<String> deleteNotLinkedImg() {
    log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", className);
    eventService.deleteNotLinkedImg();
    return ResponseEntity.ok("deleteNotLinkedImg - ok");
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
  @DeleteMapping("/events/{eventId}/cancel/{deleteRequestId}")
  public ResponseEntity<AppResponse> deleteEvent(
      @Parameter(description = "Event ID", required = true, example = "60d21b4667d0d8992e610c85")
      @PathVariable("eventId") String eventId,
      @Parameter(description = "Event delete request Id", required = true, example = "60d21b4667d0d8992e610c43")
      @PathVariable("deleteRequestId") String deleteRequestId) {
    eventService.deleteEvent(eventId, deleteRequestId);
    AppResponse response = new AppResponse(
        HttpStatus.OK.value(), "Event deleted successfully");
    log.info("Class: {}, Method: deleteEvent - Deleting event with id: {}", className, eventId);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
