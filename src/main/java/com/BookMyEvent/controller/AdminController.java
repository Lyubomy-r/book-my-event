package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.PageResponse;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.AdminService;
import com.BookMyEvent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    @Operation(
            summary = "Get paginated list of users",
            description = "Retrieves a paginated list of users. The size and page parameters specify the number of records per page and the page number, respectively.",
            parameters = {
                    @Parameter(
                            name = "size",
                            description = "The number of users per page. Default value is 10 if not specified.",
                            required = false,
                            example = "10"
                    ),
                    @Parameter(
                            name = "page",
                            description = "The page number to retrieve. Default is 0 for the first page.",
                            required = false,
                            example = "0"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of users successfully retrieved",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = PageResponse.class),
                                            examples = @ExampleObject(
                                                    name = "PageResponse",
                                                    description = """
                            Example of paginated user response:
                            - statusСode: Status code of the response.
                            - users: List of user objects on this page.
                            - totalPages: Total number of pages available.
                            - totalElements: Total number of users across all pages.
                            """,
                                                    value = """
                            {
                              "statusСode": 200,
                              "timestamp": "2023-10-08T09:32:42",
                              "users": [
                                {
                                  "id": {
                                    "timestamp": 1728405627,
                                    "date": "2024-10-08T16:40:27.000+00:00"
                                  },
                                  "name": "User1",
                                  "email": "email@te1.com",
                                  "password": "password",
                                  "mailConfirmation": false,
                                  "role": "USER",
                                  "creationDate": null,
                                  "phone": null,
                                  "location": null,
                                  "status": "ACTIVE",
                                  "savedEvents": [],
                                  "createdEvents": []
                                },
                                {
                                  "id": {
                                    "timestamp": 1728485823,
                                    "date": "2024-10-09T14:57:03.000+00:00"
                                  },
                                  "name": "User2",
                                  "email": "email@te2.com",
                                  "password": "password",
                                  "mailConfirmation": false,
                                  "role": "USER",
                                  "creationDate": null,
                                  "phone": null,
                                  "location": null,
                                  "status": "ACTIVE",
                                  "savedEvents": [],
                                  "createdEvents": []
                                }
                              ],
                              "totalPages": 5,
                              "totalElements": 50
                            }
                        """
                                            )
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid page or size parameter",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = ErrorResponseDto.class)
                                    )
                            }
                    )
            }
    )
    @GetMapping("/users/{size}/{page}")
    public ResponseEntity<PageResponse> getAllUsers(
            @PathVariable(value = "size", required = false) Integer size,
            @PathVariable(value = "page", required = false) Integer page){
        var pageData = userService.getUserPage(size,page);
        var checkPages = pageData.getTotalPages() - 1;
        if(checkPages < 0){
            checkPages = 0;
        }
        PageResponse response = new PageResponse(HttpStatus.OK.value(), pageData);
        log.info("AdminController::getAllUsers - /admin/page/{size}/{page} - Returning paginated user summary.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
                                    schema = @Schema(implementation = AppResponse.class),
                                    examples = @ExampleObject(
                                            name = "Success Response",
                                            value = """
                    {
                      "statusCode": 200,
                      "message": "User was deleted successfully."
                    }
                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid user ID or missing ID",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(
                                            name = "Invalid ID Error Response",
                                            value = """
                    {
                      "statusCode": 400,
                      "message": "User ID cannot be null or empty. 12345abcde"
                    }
                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(
                                            name = "User Not Found Response",
                                            value = """
                    {
                      "statusCode": 404,
                      "message": "User with ID 12345abcde not found"
                    }
                    """
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<AppResponse> delete(@PathVariable("userId") String userId) {
        AppResponse response = new AppResponse(
            HttpStatus.OK.value(), userService.delete(userId));
        log.info("UserController::delete - /users/{userId} - Return deletion message.");
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
                                    schema = @Schema(implementation = AppResponse.class),
                                    examples = @ExampleObject(
                                            name = "Success Response",
                                            value = """
                    {
                      "statusCode": 200,
                      "message": "User status updated to 'BANNED'"
                    }
                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "User already banned or not found",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    @PatchMapping("/banned/{email}")
    public ResponseEntity<AppResponse> banned(@PathVariable("email") String email){
        AppResponse response = new AppResponse(HttpStatus.OK.value(), adminService.banned(email));
        log.info("UserController::banned - /users/{userId} - user successfully banned.");
        return ResponseEntity.ok(response);
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
    @PatchMapping("/unbanned/{email}")
    public ResponseEntity<AppResponse> unbanned(@PathVariable("email") String email) {
        AppResponse response = new AppResponse(HttpStatus.OK.value(), adminService.unbanned(email));
        log.info("UserController::unban - /users/{userId} - user successfully unbanned.");
        return ResponseEntity.ok(response);
    }
}
