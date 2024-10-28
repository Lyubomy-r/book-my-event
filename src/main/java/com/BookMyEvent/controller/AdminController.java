package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.PageResponse;
import com.BookMyEvent.exception.model.ErrorResponseDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

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
                                            mediaType = APPLICATION_JSON_VALUE,
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
                          "timestamp": "2023-10-08 09:32:42AM",
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
                        ))}),
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
    @GetMapping("/page/{size}/{page}")
    public ResponseEntity<PageResponse> page(
            @PathVariable(value = "size", required = false) Integer size,
            @PathVariable(value = "page", required = false) Integer page){
        var pageData = userService.getPage(size,page);
        var checkPages = pageData.getTotalPages() - 1;
        if(checkPages < 0){
            checkPages = 0;
        }
        PageResponse response = new PageResponse(HttpStatus.CREATED.value(), pageData.getContent(),checkPages,pageData.getTotalElements());
        log.info("AdminController::getPage - /admin/page/{size}/{page} - Return pages message.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<AppResponse> delete(@PathVariable("userId") String userId) {
        AppResponse response = new AppResponse(
            HttpStatus.OK.value(), userService.delete(userId));
        log.info("UserController::delete - /users/{userId} - Return deletion message.");
        return ResponseEntity.ok(response);
    }
}
