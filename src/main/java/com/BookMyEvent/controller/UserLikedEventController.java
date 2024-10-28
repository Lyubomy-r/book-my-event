package com.BookMyEvent.controller;

import com.BookMyEvent.entity.UserLikedEvent;
import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.LikedEventDto;
import com.BookMyEvent.entity.dto.LikedEventResponseDto;
import com.BookMyEvent.service.UserLikedEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Tag(name = "User Liked Events", description = "API for managing liked events")
@RequestMapping("/liked-events")
@RequiredArgsConstructor
@Slf4j
public class UserLikedEventController {

  private final UserLikedEventService likedEventService;

  @Operation(
      summary = "Add a liked event",
      description = "Allows a user to add an event to their liked list.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = LikedEventDto.class),
              examples = @ExampleObject(
                  name = "LikedEventDto Example",
                  value = """
                      {
                        "userId": "12345",
                        "eventId": "67890"
                      }
                      """
              )
          )
      ),
      responses = {
          @ApiResponse(responseCode = "201", description = "Event successfully added to liked list"),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "401", description = "Unauthorized")
      }
  )
  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public ResponseEntity<AppResponse> addLikedEvent(@RequestBody LikedEventDto likedEventDto) {
    log.info("UserLikedEventController::addLikedEvent - Adding liked event for user ID: {} and event ID: {}.",
        likedEventDto.userId(), likedEventDto.eventId());

    likedEventService.addLikedEvent(likedEventDto);

    AppResponse response = new AppResponse(
        HttpStatus.CREATED.value(),
        "Liked event successfully added."
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      summary = "Remove a liked event",
      description = "Allows a user to remove an event from their liked list.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = LikedEventDto.class),
              examples = @ExampleObject(
                  name = "LikedEventDto Example",
                  value = """
                      {
                        "userId": "12345",
                        "eventId": "67890"
                      }
                      """
              )
          )
      ),
      responses = {
          @ApiResponse(responseCode = "204", description = "Event successfully removed from liked list"),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "401", description = "Unauthorized")
      }
  )
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping
  public ResponseEntity<AppResponse> removeLikedEvent(@RequestBody LikedEventDto likedEventDto) {
    log.info("Removing liked event for user ID: {} and event ID: {}.", likedEventDto.userId(), likedEventDto.eventId());

    likedEventService.removeLikedEvent(likedEventDto);

    AppResponse response = new AppResponse(
        HttpStatus.OK.value(),
        "Liked event successfully removed."
    );

    return ResponseEntity.ok(response);
  }


  @Operation(
      summary = "Get liked events for a user",
      description = "Fetch all liked events for the specified user ID.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "List of liked events",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = UserLikedEvent.class)
              )
          ),
          @ApiResponse(responseCode = "401", description = "Unauthorized")
      }
  )
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{userId}")
  public ResponseEntity<LikedEventResponseDto> getLikedEvents(@PathVariable String userId) {
    log.info("Fetching liked events for user ID: {}.", userId);

    LikedEventResponseDto likedEvents = likedEventService.getLikedEvents(userId);

    log.info("UserLikedEventController::getLikedEvents - Return liked events list found for user ID: {}.", userId);

    return ResponseEntity.ok(likedEvents);
  }

  @Operation(
      summary = "Count liked events for a user",
      description = "Retrieve the total number of liked events for the specified user ID.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Total count of liked events",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Long.class)
              )
          ),
          @ApiResponse(responseCode = "401", description = "Unauthorized")
      }
  )
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/count/{userId}")
  public ResponseEntity<Long> countLikedEvents(@PathVariable String userId) {
    log.info("Counting liked events for user ID: {}.", userId);
    long count = likedEventService.countLikedEvents(userId);
    return ResponseEntity.ok(count);
  }
}