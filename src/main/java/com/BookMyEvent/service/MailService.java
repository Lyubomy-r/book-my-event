package com.BookMyEvent.service;

public interface MailService {
    void mailSender(String emailTo);
    boolean getMessagesFromUser(String emailTo);
    void deleteOldEmails(String emailTo);
    void blockingMessage(String emailTo);
    void unblockingMessage(String emailTo);
}
