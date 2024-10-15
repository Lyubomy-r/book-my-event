package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.PasswordResetTokenRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.PasswordResetToken;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.EmailService;
import com.BookMyEvent.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final EmailService emailService;
//    private final BCryptPasswordEncoder passwordEncoder;
    @Value("${password.reset.url}")
    private String passwordResetUrl;

    @Override
    public void requestPasswordReset(String email) {
        log.info("PasswordResetServiceImpl::requestPasswordReset - Requesting password reset for email: {}", email);

        UserResponseDto user = userRepository.findUserInfoByEmail(email)
            .orElseThrow(() -> {
                log.error("PasswordResetServiceImpl::requestPasswordReset - User with such email not found: {}", email);
                return new GeneralException("User with such email not found.", HttpStatus.NOT_FOUND);
            });


        String uniqueToken = generateUniqueToken();

        PasswordResetToken token = new PasswordResetToken(user.getId());
        token.setToken(uniqueToken);
        passwordResetTokenRepository.save(token);
        log.info("PasswordResetServiceImpl::requestPasswordReset - Generated password reset token: {}", token.getToken());

        sendPasswordResetEmail(email, token.getToken());
    }


    private String generateUniqueToken() {
        String token;
        Optional<PasswordResetToken> existingToken;

        do {
            token = UUID.randomUUID().toString();
            existingToken = passwordResetTokenRepository.findByToken(token);
        } while (existingToken.isPresent());

        return token;
    }


    public void sendPasswordResetEmail(String email, String token) {

        String url = passwordResetUrl
            .replace("{token}", token);
//                    .replace("{id}", userId);

        String message = "Click the link to reset your password: " + url;
        emailService.sendSimpleMessage(email, "Password Reset Request", message);
        log.info("PasswordResetServiceImpl::sendPasswordResetEmail - Password reset link sent to email: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("PasswordResetServiceImpl::resetPassword - Attempting to reset password with token: {}", token);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                log.error("PasswordResetServiceImpl::resetPassword - Invalid token: {}", token);
                return new GeneralException("Invalid token.", HttpStatus.BAD_REQUEST);
            });

        if (resetToken.isExpired()) {
            log.error("PasswordResetServiceImpl::resetPassword - Token has expired: {}", token);
            throw new GeneralException("Token has expired.", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(resetToken.getUserId())
            .orElseThrow(() -> {
                log.error("PasswordResetServiceImpl::resetPassword - User not found for ID: {}", resetToken.getUserId());
                return new GeneralException("User not found.", HttpStatus.NOT_FOUND);
            });
        var passwordEncoder = new BCryptPasswordEncoder();
        var hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        log.info("PasswordResetServiceImpl::resetPassword - Password successfully updated for user ID: {}", user.getId());

        passwordResetTokenRepository.delete(resetToken);
        log.info("PasswordResetServiceImpl::resetPassword - Password reset token deleted: {}", token);

        emailService.sendPasswordResetConfirmationEmail(user.getEmail());
    }
}
