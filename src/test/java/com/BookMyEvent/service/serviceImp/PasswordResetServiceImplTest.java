package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.PasswordResetTokenRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.PasswordResetToken;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "password.reset.url=https://example.com/reset-password/{token}")
@Slf4j
class PasswordResetServiceImplTest {
  @Mock private UserRepository userRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private MailService emailService;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private PasswordResetServiceImpl passwordResetService;

  //  @Mock
  //  private PasswordResetTokenRepository passwordResetTokenRepository;
  @Value("${password.reset.url}")
  private String passwordResetUrl;

  final String email = "test@example.com";

  //

  @Test
  void requestPasswordReset() {}

  @Test
  void sendPasswordResetEmail() {
    String uniqueToken = UUID.randomUUID().toString();

    String passwordResetUrl = "https://evently-book.vercel.app/?renovationPassword&token={token}";
    ReflectionTestUtils.setField(passwordResetService, "passwordResetUrl", passwordResetUrl);
    String expectedUrl = passwordResetUrl.replace("{token}", uniqueToken);
    log.info("expectedUrl  {}", expectedUrl);
    //    String expectedMessage = String.format(
    //        "Привіт!\n\n"
    //            + "Ми отримали запит на зміну пароля для вашого облікового запису. Якщо це дійсно
    // ви, виконайте наступні дії:\n"
    //            + "\t1.\tНатисніть на: %s\n"
    //            + "\t2.\tВведіть новий пароль на сторінці, яка відкриється.\n"
    //            + "\t3.\tПідтвердіть пароль і натисніть \"Відновити пароль\".\n\n"
    //            + "Важливо! Посилання дійсне лише 60 хвилин.\n"
    //            + "Якщо ви не надсилали запит на скидання пароля, просто ігноруйте цей лист — ваш
    // пароль залишиться незмінним.\n\n"
    //            + "З повагою,\n"
    //            + "Команда підтримки BookMyEvent.",
    //        expectedUrl
    //    );

    passwordResetService.sendPasswordResetEmail(email, uniqueToken);

    Mockito.verify(emailService)
        .sendSimpleHtmlMailMessage6Line(
            eq(email),
            eq("Запит на відновлення пароля BookMyEvent"),
            anyString(),
            anyString(),
            ArgumentMatchers.contains(expectedUrl),
            //        eq("\t1.\tНатисніть на: " + expectedUrl),
            anyString(),
            anyString(),
            anyString(),
            anyString());
  }

  @Test
  @DisplayName(
      "Test PasswordResetServiceImpl method resetPassword. Negative Scenario password is not valid.")
  void testResetPasswordNegativeScenarioPasswordIsNotValid() {
    String token = UUID.randomUUID().toString();
    String newPassword = "newPassword";
    String errorMessage =
        "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character @$!%*?&.";
    User user =
        User.builder()
            .id(new ObjectId("66c648b600179737a3d5c235"))
            .name("Ronald")
            .email(email)
            .password("$2a$12$jbm7LHp0jWlcGE13k4gE4ufmhCOThl/5yWYydULF51XeXHz1gblUC")
            .location("Kyiv")
            .mailConfirmation(true)
            .status(Status.ACTIVE)
            .role(Role.VISITOR)
            .creationDate(LocalDateTime.now())
            .build();
    PasswordResetToken resetToken =
        new PasswordResetToken(
            "66c648b600179737a3d5c232",
            token,
            user.getId().toHexString(),
            LocalDateTime.now().plusHours(2));

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(false);
    //    when(userRepository.save(user)).thenReturn(user);
    //    doNothing().when(passwordResetTokenRepository).delete(resetToken);

    GeneralException generalException =
        assertThrows(
            GeneralException.class, () -> passwordResetService.resetPassword(token, newPassword));

    assertEquals(errorMessage, generalException.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST.value(), generalException.getHttpStatus().value());
  }

  @Test
  void isExpired() {
    LocalDateTime expirationTimeAfter = LocalDateTime.now().minusHours(1);

    LocalDateTime expirationTimeBefore = LocalDateTime.now().plusHours(1);

    assertTrue(passwordResetService.isExpired(expirationTimeAfter));
    assertFalse(passwordResetService.isExpired(expirationTimeBefore));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "newPassword",
        "newpassword!2",
        "newP12!",
        "PASWORD@56",
        "newPassword2-",
        "newPassword2##"
      })
  void testIsValidPasswordScenarioFalse(String password) {
    boolean isValidPassword = passwordResetService.isValidPassword(password);
    assertFalse(isValidPassword);
  }

  @ParameterizedTest
  @ValueSource(strings = {"newPassword2!", "Password1&", "MyStrong$Pass9", "Admin@123"})
  void testIsValidPasswordScenarioTrue(String password) {
    boolean isValidPassword = passwordResetService.isValidPassword(password);
    assertTrue(isValidPassword);
  }
}
