package com.BookMyEvent.service;

import com.BookMyEvent.entity.Ticket;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface EmailService {

//    void sendSimpleMailMessage(String name, String to);
//    void sendMimeMessageWithAttachments(String name, String to) throws MessagingException, IOException;
//    void sendMimeMessageWithEmbeddedFiles(String name, String to);
//    void sendHtmlEmailRegistration(String to) throws MessagingException, IOException;
//    void sendHtmlEmailSubscription(String to) throws MessagingException, IOException;
//
//    void sendHtmlEmailForgotPassword(String to, String password);
//
//    void sendHtmlEmailWithEmbeddedFiles(String name, String to);

    void sendBookingConfirmation(String to, Ticket ticket);

    void sendPasswordResetEmail(String email, String token);

    void sendSimpleMessage(String email, String subject, String message);

    void sendPasswordResetConfirmationEmail(String email);

}