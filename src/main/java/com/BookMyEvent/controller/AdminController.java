package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/main")
    public String admin() {
        return "hi admin";
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<AppResponse> delete(@PathVariable("userId") String userId) {
        AppResponse response = new AppResponse(
            HttpStatus.OK.value(), userService.delete(userId));
        log.info("UserController::delete - /users/{userId} - Return deletion message.");
        return ResponseEntity.ok(response);
    }
}
