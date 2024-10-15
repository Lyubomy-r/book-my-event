package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.MailConfirmationRepository;
import com.BookMyEvent.entity.UserEmailData;
import com.BookMyEvent.service.MailService;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.security.SecureRandom;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImp implements MailService {

    @Value("${cloud.server.url}")
    private String serverUrl;

    private final MailConfirmationRepository mailRepository;

    private final HttpServletRequest request;

    public void mailSender(String emailTo) {
        var from = "bookmyevent037@gmail.com";
//        var to = "willyosho3@gmail.com";
        var host = "smtp.gmail.com";
        var port = "465";

        var props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");

        var session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, "fjle xkuc crdq ktiz");
            }
        });
        session.setDebug(true);
        try {
            var password = randomPasswordGenerator();

            var baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            log.info("baseUrl : {}", baseUrl);
            log.info("serverUrl : {}", serverUrl);
            var url = serverUrl+"/api/v1/authorize/mail-confirmation/" + emailTo + "/" + password;
            log.info("verify url : {}", url);
            var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
            message.setSubject("Реєстрація на сайті BookMyEvent");
            message.setText("Привіт!\uD83C\uDF89\n" +
                "Дякуємо, що приєднався до BookMyEvent! Щоб завершити реєстрацію, просто натисни на цей лінк:\n" +
                url + "\n" + "\n" +
                "Тепер ти готовий створити свою першу подію або забронювати білет на класний івент! Чекаємо на тебе!");
            Transport.send(message);

            var passwordEncoder = new BCryptPasswordEncoder();

            var hashedPassword = passwordEncoder.encode(password);
            mailRepository.save(new UserEmailData(emailTo,hashedPassword));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String randomPasswordGenerator(){
        var UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        var LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
        var DIGITS = "0123456789";
        var SPECIAL_CHARACTERS = "!&*()";

        var ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
        var RANDOM = new SecureRandom();
        var password = new StringBuilder(8);

        password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARACTERS.charAt(RANDOM.nextInt(SPECIAL_CHARACTERS.length())));

        for (int i = 4; i < 8; i++) {
            password.append(ALL_CHARACTERS.charAt(RANDOM.nextInt(ALL_CHARACTERS.length())));
        }
        return password.toString();
    }
}
