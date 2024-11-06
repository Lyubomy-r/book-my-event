package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j

public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${front.url}")
    private String frontUrl;


    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        log.info("EmailServiceImpl::sendPasswordResetEmail - Sending password reset email to: {}", email);
        try {
            String messageBody = buildPasswordResetEmailBody(token);

            sendSimpleMessage(email, "Password Reset Request", messageBody);
            log.info("EmailServiceImpl::sendPasswordResetEmail - Email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("EmailServiceImpl::sendPasswordResetEmail - Error sending email to: {}. Exception: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendBookingConfirmation(String to, Ticket ticket) {
        log.info("EmailServiceImpl::sendBookingConfirmation - Sending booking confirmation email to: {}", to);
        try {
            String emailBody = buildEmailBody(ticket);

            sendSimpleMessage(to, "Booking Confirmation - Your Ticket", emailBody);
            log.info("EmailServiceImpl::sendBookingConfirmation - Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("EmailServiceImpl::sendBookingConfirmation - Error sending email to: {}. Exception: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send booking confirmation email", e);
        }
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String message) {
        log.info("EmailServiceImpl::sendSimpleMessage - Sending email to: {}", to);
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            javaMailSender.send(mailMessage);
            log.info("EmailServiceImpl::sendSimpleMessage - Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("EmailServiceImpl::sendSimpleMessage - Error sending email to: {}. Exception: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendPasswordResetConfirmationEmail(String email) {
        log.info("EmailServiceImpl::sendPasswordResetConfirmationEmail - Sending password reset confirmation email to: {}", email);
        String message = String.format("Привіт!\n\n" +
            "Ваш пароль було успішно оновлено.\n" +
            "Тепер ви можете увійти до свого облікового запису за допомогою нового пароля: (%s).\n\n" +
            "Якщо ви не запитували зміну пароля, будь ласка, зверніться до нашої служби підтримки.\n\n" +
            "З повагою,\n"
            + "Команда підтримки BookMyEvent.", frontUrl);


        sendSimpleMessage(email, "Пароль оновлено", message);
    }

    private String buildEmailBody(Ticket ticket) {
        log.info("EmailServiceImpl::buildEmailBody - Building email body for ticket: {}", ticket);
        return String.format(
                "Dear Customer,%n%n" +
                        "Thank you for booking with us!%n%n" +
                        "Here are your booking details:%n" +
                        "Event: %s%n" +
                        "Seat: Row %d, Seat %d%n" +
                        "Booking Date: %s%n" +
                        "Reservation Expires: %s%n%n" +
                        "We look forward to seeing you at the event!%n%n" +
                        "Best regards,%n" +
                        "The Booking Team",
                ticket.getTitle(),
                ticket.getRow(),
                ticket.getSeat(),
                ticket.getBuyingDate(),
                ticket.getReservationExpires()
        );
    }

    private String buildPasswordResetEmailBody(String token) {
        log.info("EmailServiceImpl::buildPasswordResetEmailBody - Building email body for password reset token: {}", token);
        return String.format(
                "Dear User,%n%n" +
                        "We received a request to reset your password.%n%n" +
                        "Please click the link below to reset your password:%n" +
                        "http://yourapp.com/reset-password?token=%s%n%n" +
                        "If you did not request this, please ignore this email.%n%n" +
                        "Best regards,%n" +
                        "The Support Team",
                token
        );
    }
}
