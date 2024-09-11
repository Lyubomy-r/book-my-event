package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.MailConfirmationRepository;
import com.BookMyEvent.entity.UserEmailData;
import com.BookMyEvent.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImp implements MailService {

    private final MailConfirmationRepository mailRepository;

    public void mailSender(String emailTo) {
        var from = "bookmyevent037@gmail.com";
        var to = "willyosho3@gmail.com";
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
            var url = "http://localhost:8080/api/v1/authorize/mail-confirmation/" + emailTo + "/" + password;
            var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("регестрація на сайті BookMyEvent");
            message.setText("підтвердіть свою пошту, перейдіть по силці " + url);
            Transport.send(message);
            // Create a BCryptPasswordEncoder instance
            var passwordEncoder = new BCryptPasswordEncoder();

            // Hash the password
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
