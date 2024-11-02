package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.MailConfirmationRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EmailVerificationResponseDTO;
import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.entity.dto.LoginResponse;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.AuthService;
import com.BookMyEvent.service.MailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImp implements AuthService {
    @Value("${jwt.signing.key}")
    private String signingKey;

    private final UserMapper userMapper;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final MailConfirmationRepository mailRepository;

    @Override
    public String userRegistration(UserSaveDto userData) {
        log.info("AuthServiceImp::userRegistration. Start method");
        var checkEmail = repository.findUserByEmail(userData.getEmail());
        if (checkEmail.isPresent()) {
            if (!checkEmail.get().isMailConfirmation()) {
                log.warn("AuthServiceImp::userRegistration. Return error message.");
                throw new GeneralException(
                    String.format("This email address (%s) already exists and needs confirmation.",
                        checkEmail.get().getEmail()),
                    HttpStatus.BAD_REQUEST);
            }
            log.warn("AuthServiceImp::userRegistration. Return error message.");
            throw new GeneralException("Email is already in use.", HttpStatus.BAD_REQUEST);
        } else {
            mailService.mailSender(userData.getEmail());
            var hashedPassword = passwordEncoder.encode(userData.getPassword());
            userData.setPassword(hashedPassword);
            User newUser = userMapper.toUserFromUserSaveDto(userData);
            LocalDateTime timeCreate = LocalDateTime.now();
            ZonedDateTime kyivTime = ZonedDateTime.now(ZoneId.of("Europe/Kiev"));
            log.info("timeCreate "+timeCreate);
            log.info("kyivTime "+kyivTime);
            newUser.setCreationDate(timeCreate);
            newUser.setMailConfirmation(false);
            newUser.setRole(Role.VISITOR);
            newUser.setStatus(Status.ACTIVE);
            repository.save(newUser);
            String response = "User registered successfully.";
            mailService.deleteOldEmails(userData.getEmail());
            log.info("AuthServiceImp::userRegistration. Return message ({}).", response);
            return response;
        }
    }
    @Override
    public EmailVerificationResponseDTO checkExistEmail(String email) {
        Boolean checkExistEmail = repository.existsByEmail(email);
        if(checkExistEmail){
            EmailVerificationResponseDTO response = new EmailVerificationResponseDTO(
                HttpStatus.OK.value(), String.format("Email (%s) is already in use.",email), checkExistEmail);
            log.info("AuthServiceImp::checkExistEmail. Return check if Exist Email ({}) Boolean ({}).", email, checkExistEmail);
            return response ;
        }else {
            EmailVerificationResponseDTO response = new EmailVerificationResponseDTO(
                HttpStatus.OK.value(), String.format("Email (%s) is not use.", email), checkExistEmail);
            log.info("AuthServiceImp::checkExistEmail. Return check if Exist Email ({}) Boolean ({}).", email, checkExistEmail);
            return response ;
        }
    }
    @Override
    public String emailVerificationCheck(String email, String password) {

        var user = repository.findUserByEmail(email).orElseThrow(() -> {
            log.warn("AuthServiceImp::emailVerificationCheck. Return error message.");
            return new GeneralException(String.format("User not found email (%s).", email), HttpStatus.NOT_FOUND);
        });

        var userEmailData = mailRepository.findByEmail(email);
        String response;
        if(userEmailData != null){
            if (passwordEncoder.matches(password, userEmailData.getEmailCode())) {
                user.setMailConfirmation(true);

                repository.save(user);
                mailRepository.delete(userEmailData);

               response = String.format("Email (%s) is confirmed", email);
                log.info("AuthServiceImp::emailVerificationCheck. user confirmed email.");
                return response;
            }
            else {
                log.warn("AuthServiceImp::emailVerificationCheck. the user entered incorrect data.Message (Wrong password)");
                return "Wrong password";
            }
        }
        else {
             response = String.format("Email (%s) not found", email);
            log.warn("AuthServiceImp::emailVerificationCheck. Email not found.");
            return response;
        }
    }

    private String generateToken(Authentication authentication, Role role) {
        var token = Jwts.builder()
            .setSubject(authentication.getName())
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + 1000 * 60 * 60 * 10))
            .signWith(SignatureAlgorithm.HS512, signingKey)
            .compact();
        log.info("AuthServiceImp::generateToken. Role JWT to Role ({}).", role);
        log.info("AuthServiceImp::generateToken. Generate JWT to user ({}).", authentication.getName());
        return token;
    }

    @Override
    public LoginResponse login(LoginDto loginData) {
        var user = repository.findUserByEmail(loginData.getEmail());
        if (user.isPresent()) {
            if (user.get().isMailConfirmation()) {
                if (passwordEncoder.matches(loginData.getPassword(), user.get().getPassword())) {
                    var authentication = SecurityContextHolder.getContext().getAuthentication();
                    var token = generateToken(authentication, user.get().getRole());
                    LoginResponse tokenPair = new LoginResponse(user.get().getId().toHexString(),
                        user.get().getName(), token,
                        String.format("Email (%s) is confirmed",
                            loginData.getEmail()),
                        HttpStatus.OK.value());
                    log.info("AuthServiceImp::login. Verified and return jwt token to user ({}).", loginData.getEmail());
                    return tokenPair;
                } else {
                    log.warn("AuthServiceImp::login. Return  message: Wrong password");
//                    return new LoginResponse(user.get().getId().toHexString(),
//                        user.get().getName(),
//                        "Wrong password",
//                        HttpStatus.BAD_REQUEST.value());
                    throw new GeneralException("Wrong password", HttpStatus.BAD_REQUEST);
                }
            } else {
                log.warn("AuthServiceImp::login. Return  message.Confirm your email ({})", loginData.getEmail());
//                return new LoginResponse(user.get().getId().toHexString(),
//                    user.get().getName(),
//                    String.format("Confirm your email (%s)", loginData.getEmail()),
//                    HttpStatus.UNAUTHORIZED.value());

                throw new GeneralException(String.format("Confirm your email (%s)", loginData.getEmail()),
                    HttpStatus.UNAUTHORIZED);

            }
        } else {
            log.warn("AuthServiceImp::login. Return error message: Email is not registered");
//            return new LoginResponse(
//                String.format("Email (%s) is not registered.", loginData.getEmail()),
//                HttpStatus.BAD_REQUEST.value());

            throw new GeneralException(String.format("Email (%s) is not registered.", loginData.getEmail()),
                HttpStatus.BAD_REQUEST);
        }
    }

    public String sendLetterToUser(String email) {
        log.info("Checking if email was previously sent to user: {}", email);

        var containsMessage = mailService.getMessagesFromUser(email);

        if (containsMessage) {
            log.info("User {} has already received a letter with the required content.", email);
            return "The user has such a letter in their correspondence.";
        } else {
            log.info("User {} has not received the letter. Sending message again.", email);
            mailService.mailSender(email);
            return "The message was sent to the user again.";
        }
    }
}
