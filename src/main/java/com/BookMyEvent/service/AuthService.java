package com.BookMyEvent.service;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.LoginDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {
    public ResponseEntity<String> userRegistration(@RequestBody User userData);

    public ResponseEntity<String> emailVerificationCheck(@PathVariable String email, @PathVariable String password);

    public ResponseEntity<String> login(LoginDto loginData);
}
