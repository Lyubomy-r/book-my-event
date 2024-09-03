package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.MailConfirmationRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.UserEmailData;
import com.BookMyEvent.service.SingInServiceInterface;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SingInService implements SingInServiceInterface {

    private  UserRepository repository;

    @Autowired
    public SingInService(UserRepository repository) {
        this.repository = repository;
    }

    @Autowired
    private MailConfirmationRepository mailRepository;
    public String UserRegistration(@RequestBody User userData) {
        var checkEmail = repository.findUserInfoByEmail(userData.getEmail());
        if (checkEmail != null) {
            if (!checkEmail.isMailConfirmation()){
                return "Ця електронна адреса вже існує, її потрібно підтвердити.";
            }
            return "Електронна пошта вже використовується";
        }
        else{
            MailSender(userData.getEmail());
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            String hashedPassword = passwordEncoder.encode(userData.getPassword());
            userData.setPassword(hashedPassword);
            repository.save(userData);
            return "Registration successful";
        }
    }
    public List<User> GetUsers(){
       return repository.findAll();
    }

    private void MailSender(String emailTo) {
        String from = "bookmyevent037@gmail.com";
        String to = "willyosho3@gmail.com";
        String host = "smtp.gmail.com";
        String port = "465";

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, "fjle xkuc crdq ktiz");
            }
        });
        session.setDebug(true);
        try {
            String password = RandomPasswordGenerator();
            String url = "http://localhost:8080/EmailVerificationCheck/" + emailTo + "/" + password;
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("регестрація на сайті BookMyEvent");
            message.setText("підтвердіть свою пошту, перейдіть по силці " + url);
            Transport.send(message);
            // Create a BCryptPasswordEncoder instance
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            // Hash the password
            String hashedPassword = passwordEncoder.encode(password);
            mailRepository.save(new UserEmailData(hashedPassword,emailTo));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String RandomPasswordGenerator(){
         String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
         String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
         String DIGITS = "0123456789";
         String SPECIAL_CHARACTERS = "!@#$%^&*()-_";

         String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
         SecureRandom RANDOM = new SecureRandom();
            StringBuilder password = new StringBuilder(8);

            password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
            password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
            password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
            password.append(SPECIAL_CHARACTERS.charAt(RANDOM.nextInt(SPECIAL_CHARACTERS.length())));

            for (int i = 4; i < 8; i++) {
                password.append(ALL_CHARACTERS.charAt(RANDOM.nextInt(ALL_CHARACTERS.length())));
            }

            return password.toString();

    }

    public String EmailVerificationCheck(@PathVariable String email, @PathVariable String password) {
        User  user = repository.findUserInfoByEmail(email);
        UserEmailData userEmailData = mailRepository.findByEmail(email);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (passwordEncoder.matches(password, userEmailData.getEmailCode())) {
            user.setMailConfirmation(true);
            return "Електронну пошту підтверджено";
        }
        else {
            return "Електронну пошту не підтверджено";
        }
    }

    public String generateToken(Authentication authentication) {
        String token = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("role", Role.USER )  // Добавляем роль в токен
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + 1000 * 60 * 60 * 10)) // 10 часов
                .signWith(SignatureAlgorithm.HS512, "SecretKeyToGenJWTs")
                .compact();
        return token;
    }

    public String Login(String email, String password) {
        User user = repository.findUserInfoByEmail(email);
        if (user != null) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(password, user.getPassword())) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                return generateToken(authentication);
            }
            else {
                return "логін або пароль введено невірно";
            }
        }
        else {
            return "логін або пароль введено невірно";
        }
    }
}
