package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.EmailVerificationResponseDTO;
import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.entity.dto.LoginResponse;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.serviceImp.AuthServiceImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Tag(name = "Authentication & Authorization")
@RequestMapping("/authorize")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImp service;

    @Operation(
        summary = "User signup",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserSaveDto.class),
                examples = @ExampleObject(
                    name = "UserSaveDto",
                    description = """
                Example of User Signup:
                - Name: Must contain 3-40 characters, only alphabetic characters are allowed. Cannot be empty.
                - Email: Must be a valid email address. Cannot be empty.
                - Password: Must be at least 8 characters long and contain an lowercase letter,uppercase letter, a number, and a special character. Cannot be empty.
                """,
                    value = """
                {
                  "name": "John Doe",
                  "email": "johndoe@example.com",
                  "password": "P@ssw0rd"
                }
                """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "User registration successfully",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = AppResponse.class))
                }),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request or Validation failed",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponseDto.class))
                })
        })
    @PostMapping("/registration")
    public ResponseEntity<AppResponse> registration(@Valid @RequestBody UserSaveDto newUser) {
        AppResponse response = new AppResponse(
            HttpStatus.CREATED.value(), service.userRegistration(newUser));
        log.info("AuthController::registration - /registration - Saved a new user about with email {}", newUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mail-confirmation/{email}/{password}")
    public ResponseEntity<AppResponse> mailConfirmation(@PathVariable String email, @PathVariable String password) {
      var response = service.emailVerificationCheck(email,password);
      String encodedMessage = URLEncoder.encode(response, StandardCharsets.UTF_8);
      log.info("AuthController::mailConfirmation - /registration - return mail confirmation message with email {}", email);
      return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("https://sergiy5.github.io/evently_front/?emailConfirmed=true&message=" + encodedMessage + "&email=" + email)).build();

    }

    @Operation(
        summary = "User login",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = LoginDto.class),
                examples = @ExampleObject(
                    name = "LoginDto",
                    description = """
                Example of User login:
                - Email: Must be a valid email address. Cannot be empty.
                - Password: Must be at least 8 characters long and contain an lowercase letter, uppercase letter, a number, and a special character. Cannot be empty.
                """,
                    value = """
                {
                  "email": "johndoe@example.com",
                  "password": "P@ssw0rd"
                }
                """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = LoginResponse.class))
                }),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid user email, password.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponseDto.class))
                })
        })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDto loginData) {
      LoginResponse response = service.login(loginData);
        log.info("AuthController::login - /login - return jwt with email {}", loginData.getEmail());
        return ResponseEntity.ok(response);
    }

  @Operation(
      summary = "Check if email exist.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Is Email used.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = EmailVerificationResponseDTO.class))
              })
      })
    @GetMapping("/exist/{email}")
  public ResponseEntity<EmailVerificationResponseDTO> checkExistEmail(@PathVariable String email ){
    EmailVerificationResponseDTO response = service.checkExistEmail(email);
    log.info("AuthController::checkExistEmail - /registration - return mail confirmation message with email {}", email);
    return ResponseEntity.ok(response);
  }


}
