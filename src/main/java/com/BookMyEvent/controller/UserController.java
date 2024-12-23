package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponseDto> findUserInfoById(@PathVariable("userId") String userId) {
    UserResponseDto userResponse = userService.findUserInfoById(userId);
    log.info("UserController::findUserInfoById - /users/{userId} - Return User Info email: {} .", userResponse.getEmail());
    return ResponseEntity.ok(userResponse);
  }

  @Operation(
      summary = "Update USER Fields.",
      description = "Update USER Fields in user account. Except password and roles and status.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = @Content(
              mediaType = APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = UserUpdateDto.class),
              examples = @ExampleObject(
                  name = "UserUpdateDto",
                  description = """
                Example of User Update:
                - Name: Must contain 2-50 characters, only alphabetic characters are allowed. Cannot be empty. 
                        Example: "John".
                - Surname: Must contain 2-50 characters, only alphabetic characters are allowed. Cannot be empty. 
                           Example: "Doe".
                - Email: Must be a valid email address. Cannot be empty. 
                         Example: "john.doe@example.com".
                - Birthday Date: Must be a valid date and time in ISO-8601 format. Can be null. 
                                 Example: "2000-01-01T00:00:00".
                - Location: Optional field to specify user location. Can be null. 
                            Example: "New York, USA".
                - Phone Number: Must contain 10-12 digits and can include an optional "+" prefix. Cannot be empty. 
                                Example: "+12345678901".
                """,
                  value = """
                            {
                               "name": "John",
                               "surname": "Doe",
                               "email": "john.doe@example.com",
                               "birthdayDate": "1990-05-15",
                               "location": "New York, USA",
                               "phoneNumber": "+12345678901"
                             }
                      """
              )
          )
      ),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = " Return Updated USER.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = UserResponseDto.class)
                  )}
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Bad request or not Validation failed",
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
  @PatchMapping("/{userId}")
  @PreAuthorize("#userId == authentication.principal['id']")
  public ResponseEntity<UserResponseDto> updateFields(@PathVariable("userId") String userId,
                                                      @RequestBody UserUpdateDto userUpdateDto) {
    UserResponseDto userResponse = userService.updateFieldsFromAdmin(userId, userUpdateDto);
    log.info("UserController::updateFieldsFromAdmin - /users/{userId} - Return updated User Info email: {} .", userResponse.getEmail());
    return ResponseEntity.ok(userResponse);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<AppResponse> delete(@PathVariable("userId") String userId) {
    AppResponse response = new AppResponse(
        HttpStatus.OK.value(), userService.delete(userId));
    log.info("UserController::delete - /users/{userId} - Return deletion message.");
    return ResponseEntity.ok(response);
  }
}
