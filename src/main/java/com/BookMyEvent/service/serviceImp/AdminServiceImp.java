package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.AdminService;
import com.BookMyEvent.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImp implements AdminService {

    private final UserRepository userRepository;
    private final MailService mailService;

    @Override
    public String banned(String email) {
        log.info("Attempting to ban user with email: {}", email);

        var userOptional = userRepository.findUserByEmail(email);
        if (userOptional.isPresent()) {
            var user = userOptional.get();
            log.info("User found: {} with current status: {}", user.getEmail(), user.getStatus());

            if (!user.getStatus().equals(Status.BANNED)) {
                mailService.blockingMessage(user.getEmail());
                user.setStatus(Status.BANNED);
                userRepository.save(user);
                log.info("User status updated to 'BANNED' for user: {}", user.getEmail());
                return "User status updated to 'BANNED'";
            } else {
                log.warn("User with email: {} is already banned.", email);
                throw new GeneralException("User is already banned", HttpStatus.BAD_REQUEST);
            }
        } else {
            log.warn("No user found with email: {}", email);
            throw new GeneralException(
                    String.format("User with such email: (%s) not found", email),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public String unbanned(String email) {
        log.info("Attempting to activate user with email: {}", email);

        var userOptional = userRepository.findUserByEmail(email);
        if (userOptional.isPresent()) {
            var user = userOptional.get();
            log.info("User found: {} with current status: {}", user.getEmail(), user.getStatus());

            if (!user.getStatus().equals(Status.ACTIVE)) {
                mailService.unblockingMessage(user.getEmail());
                user.setStatus(Status.ACTIVE);
                userRepository.save(user);
                log.info("User status successfully updated to 'ACTIVE' for user: {}", user.getEmail());
                return "User activated successfully";
            } else {
                log.warn("User with email: {} is already active.", email);
                return "User is already active";
            }
        } else {
            log.warn("No user found with email: {}", email);
        }

        log.info("Activation operation for user with email {} completed with result: 'User not found'", email);
        return "User not found";
    }
}
