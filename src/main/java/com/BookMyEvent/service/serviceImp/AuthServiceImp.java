package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.MailConfirmationRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.service.AuthService;
import com.BookMyEvent.service.MailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImp implements AuthService {
//    @Value("${jwt.signing.key}")
//    private String signingKey;
    private final UserRepository repository;

    private final MailService mailService;
    private final MailConfirmationRepository mailRepository;

    public ResponseEntity<String> userRegistration(User userData) {
        var checkEmail = repository.findUserByEmail(userData.getEmail());
        if (checkEmail.isPresent()) {
            if (!checkEmail.get().isMailConfirmation()){
                return ResponseEntity.badRequest().body("This email address already exists and needs to be verified.");
            }
            return ResponseEntity.badRequest().body("Email is already in use.");
        }
        else{
            mailService.mailSender(userData.getEmail());
            var passwordEncoder = new BCryptPasswordEncoder();
            var hashedPassword = passwordEncoder.encode(userData.getPassword());
            userData.setPassword(hashedPassword);
            repository.save(userData);
            return ResponseEntity.ok("User registered successfully");
        }
    }

    public ResponseEntity<String> emailVerificationCheck(String email, String password) {
        var  user = repository.findUserByEmail(email);
        if (user.isPresent()){
            var userEmailData = mailRepository.findByEmail(email);
            var passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(password, userEmailData.getEmailCode())) {
                user.get().setMailConfirmation(true);
                var existingUser = user.get();
                repository.save(existingUser);
                return ResponseEntity.ok("Email is confirmed");
            }
            else {
                return ResponseEntity.badRequest().body("Email not verified");
            }
        }
        else {
            return ResponseEntity.badRequest().body("Email not found");
        }

    }

    private String generateToken(Authentication authentication) {
        var token = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("role", Role.USER )  // Добавляем роль в токен
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + 1000 * 60 * 60 * 10)) // 10 часов
                .signWith(SignatureAlgorithm.HS512, "SecretKeyToGenJWTs")
                .compact();
        return token;
    }

    public ResponseEntity<String> login(LoginDto loginData ) {
        var user = repository.findUserByEmail(loginData.getEmail());
        if (user.isPresent()) {
            if(user.get().isMailConfirmation()){
                var passwordEncoder = new BCryptPasswordEncoder();
                if (passwordEncoder.matches(loginData.getPassword(), user.get().getPassword())) {
                    var authentication = SecurityContextHolder.getContext().getAuthentication();
                    var token = generateToken(authentication);
                    return ResponseEntity.ok().body(token);
                }
                else {
                    return ResponseEntity.badRequest().body("the login or password is entered incorrectly");
                }
            }
            else {
                return ResponseEntity.badRequest().body("confirm your email");
            }
        }
        else {
            return ResponseEntity.badRequest().body("the login or password is entered incorrectly");
        }
    }
}
