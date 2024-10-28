package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.PasswordResetTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

  @InjectMocks
  private PasswordResetServiceImpl passwordResetService;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Test
  void requestPasswordReset() {
  }

  @Test
  void sendPasswordResetEmail() {
  }

  @Test
  void resetPassword() {
  }

  @Test
  void isExpired() {
    LocalDateTime expirationTimeAfter = LocalDateTime.now().minusHours(1);

    LocalDateTime expirationTimeBefore = LocalDateTime.now().plusHours(1);

    assertTrue(passwordResetService.isExpired(expirationTimeAfter));
    assertFalse(passwordResetService.isExpired(expirationTimeBefore));
  }
}