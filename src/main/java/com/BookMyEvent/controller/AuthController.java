package com.BookMyEvent.controller;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.service.serviceImp.AuthServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/authorize")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImp service;


    @PostMapping("/registration")
    public ResponseEntity<String> registration(@Valid @RequestBody UserSaveDto newUser) {
        var user = new User(newUser.getName(), newUser.getEmail(), newUser.getPassword(), newUser.isMailConfirmation(), newUser.getRole(), LocalDateTime.now(), newUser.getLocation());
        log.info("ReviewController::save - /reviews - Saved a new review about the user {}", newUser.getEmail());
        return  service.userRegistration(user);
    }


    @GetMapping("/mail-confirmation/{email}/{password}")
    public ResponseEntity<String> mailConfirmation(@PathVariable String email,@PathVariable String password) {
            log.info("ReviewController::save - /reviews - user verified {}", email);

        return service.emailVerificationCheck(email,password);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDto loginData) {
        log.info("ReviewController::save - /user requests login {}", loginData.getEmail());
        return service.login(loginData);
    }

}
