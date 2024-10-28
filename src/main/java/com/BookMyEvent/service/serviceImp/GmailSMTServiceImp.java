package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.File;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Primary
public class GmailSMTServiceImp  {
  public static final String SUB_REGISTRATION = "Дякуємо за реєстрацію в магазині Sport Are.";
  public static final String SUB_SUBSCRIPTION = "Дякуємо за підписку в магазині Sport Are.";
  public static final String SUB_FORGOT_PASSWORD = "Відновлення паролю в магазині Sport Are.";
  public static final String UTF_8_ENCODING = "UTF-8";
  public static final String TEMPLATE_SUBSCRIPTION = "email-template-subscription";
  public static final String TEMPLATE_REGISTRATION = "email-template-registration";
  public static final String TEMPLATE_FORGOT_PASSWORD = "email-template-forgot-password";
  public static final String TEXT_HTML_ENCONDING = "text/html";

  private final JavaMailSender emailSender;

  private final TemplateEngine templateEngine;

//    @Value("${spring.mail.verify.host}")
//    private String host;

  @Value("${spring.mail.username}")
  private String fromEmail;

//  @Override
  public void sendSimpleMailMessage(String name, String to) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
//            message.setSubject();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setText("Test simple mail send");
      emailSender.send(message);
    } catch (Exception exception) {
      System.out.println(exception.getMessage());
      throw new RuntimeException(exception.getMessage());
    }
  }

//  @Override
  public void sendMimeMessageWithAttachments(String name, String to) {

    try {
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
      helper.setPriority(1);
//            helper.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setText("Test simple mail send");
      //Add attachments
      FileSystemResource fort = new FileSystemResource(new File(System.getProperty("user.home") + "/Downloads/images/fort.jpg"));
      FileSystemResource dog = new FileSystemResource(new File(System.getProperty("user.home") + "/Downloads/images/dog.jpg"));
      FileSystemResource homework = new FileSystemResource(new File(System.getProperty("user.home") + "/Downloads/images/homework.docx"));
      helper.addAttachment(fort.getFilename(), fort);
      helper.addAttachment(dog.getFilename(), dog);
      helper.addAttachment(homework.getFilename(), homework);
      emailSender.send((MimeMessagePreparator) message);
    } catch (Exception exception) {
      System.out.println(exception.getMessage());
      throw new RuntimeException(exception.getMessage());
    }

  }

//  @Override
  public void sendMimeMessageWithEmbeddedFiles(String name, String to) {

  }

//  @Override
  public void sendHtmlEmailRegistration(String to) {

    try {
      Context context = new Context();
//            context.setVariables(Map.of("name", name));
      String text = templateEngine.process(TEMPLATE_REGISTRATION, context);
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
      helper.setPriority(1);
      helper.setSubject(SUB_REGISTRATION);
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setText(text, true);
      emailSender.send(message);
    } catch (Exception exception) {
      System.out.println(exception.getMessage());
      throw new RuntimeException(exception.getMessage());
    }

  }

//  @Override
  public void sendHtmlEmailSubscription(String to) {

    try {
      Context context = new Context();
//            context.setVariables(Map.of("name", "Jod"));
      String text = templateEngine.process(TEMPLATE_SUBSCRIPTION, context);
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
      helper.setPriority(1);
      helper.setSubject(SUB_SUBSCRIPTION);
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setText(text, true);
      emailSender.send(message);
    } catch (Exception exception) {
      System.out.println(exception.getMessage());
      throw new RuntimeException(exception.getMessage());
    }
  }

//  @Override
  public void sendHtmlEmailForgotPassword(String to, String password) {

    try {
      Context context = new Context();
      context.setVariables(Map.of("password", password));
      String text = templateEngine.process(TEMPLATE_FORGOT_PASSWORD, context);
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
      helper.setPriority(1);
      helper.setSubject(SUB_FORGOT_PASSWORD);
      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setText(text, true);
      emailSender.send(message);
    } catch (Exception exception) {
      System.out.println(exception.getMessage());
      throw new RuntimeException(exception.getMessage());
    }

  }

//  @Override
  public void sendHtmlEmailWithEmbeddedFiles(String name, String to) {

  }

  private MimeMessage getMimeMessage() {
    return emailSender.createMimeMessage();
  }

  private String getContentId(String filename) {
    return "<" + filename + ">";
  }
}


