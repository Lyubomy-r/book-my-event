package com.BookMyEvent.service;

public interface MailService {
//    void mailSenderAfterRegistration(String emailTo);
    boolean getMessagesFromUser(String emailTo);
    void deleteOldEmails(String emailTo);
    void blockingMessage(String emailTo);
    void unblockingMessage(String emailTo);

    void sendHtmlEmailAfterRegistration(String emailTo);

    void sendSimpleHtmlMailMessage4Line(String emailTo,
                                        String subject,
                                        String title,
                                        String messageText1,
                                        String messageText2,
                                        String messageText3,
                                        String messageText4);

    void sendSimpleHtmlMailMessage6Line(String emailTo,
                                        String subject,
                                        String title,
                                        String messageText1,
                                        String messageText2,
                                        String messageText3,
                                        String messageText4,
                                        String messageText5,
                                        String messageText6);

    void sendSimpleHtmlMailMessageAfterBuyTicket(
            String emailTo,
            String subject,
            String title,
            String messageText1,
            String messageText2,
            String messageText3,
            String messageText4,
            String messageText5,
            String messageText6,
            String messageText7,
            String imageUrl,
            String userCabinetUrl);

    String replaceTextToLinkWithHtml(String text, String urlToEvent);
}
