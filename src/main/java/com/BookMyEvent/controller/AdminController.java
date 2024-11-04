package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.PageResponse;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Tag(name = "Admin Endpoints")
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

  private final UserService userService;

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
  public ResponseEntity<List<UserResponseDto>> findAllUserProfiles() {
    List<UserResponseDto> userList = userService.findAllUserProfiles();
    log.info("{}::findAllUsers - /admin/users - Return list of user.", this.getClass().getSimpleName());
    return ResponseEntity.ok(userList);
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
        HttpStatus.OK.value(), userService.delete(userId));
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

}
