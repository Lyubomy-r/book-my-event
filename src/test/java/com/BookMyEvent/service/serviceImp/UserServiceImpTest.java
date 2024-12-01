package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.MailService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class UserServiceImpTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private MailService mailService;

  @InjectMocks
  private UserServiceImp userService;

  private User userOne;

  @BeforeEach
  void createUserOne() {

    userOne = User.builder()
        .id(new ObjectId("66c648b600179737a3d5c235"))
        .name("Ronald")
        .email("sewewt@code.com")
        .password("As123ertyuer")
        .location("Kyiv")
        .mailConfirmation(true)
        .status(Status.ACTIVE)
        .role(Role.VISITOR)
        .creationDate(LocalDateTime.now())
        .build();
  }



  @Nested
  @DisplayName("Test UserService method Banned")
  class UserBannedTests {
    @Test
    @DisplayName("Test UserService method Banned Positive Scenario")
    void testMethodBannedPositiveScenario() {
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));
      when(userRepository.save(userOne)).thenReturn(userOne);
      doNothing().when(mailService).sendSimpleHtmlMailMessage4Line(anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString());
      String user = userService.banned(userOne.getEmail());

      assertEquals("User status updated to 'BANNED'", user);
    }

    @Test
    @DisplayName("Test UserService method Banned Negative Scenario. User with email not found.")
    void testMethodBannedNegativeScenarioNotFound() {
      String notExistEmail = "sewewtnot@code.com";
      when(userRepository.findUserByEmail(notExistEmail)).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.banned(notExistEmail));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals(String.format("User with such email: (%s) not found", notExistEmail),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(notExistEmail);
      verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Test UserService method Banned Negative Scenario. User is already banned")
    void testMethodBannedNegativeScenarioAlreadyBanned() {
      userOne.setStatus(Status.BANNED);
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.banned(userOne.getEmail()));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals("User is already banned", errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(userOne.getEmail());
      verify(userRepository, times(0)).save(any());
    }


  }

  @Nested
  @DisplayName("Test UserService method Unban.")
  class UserUnbanTests {

    @Test
    @DisplayName("Test UserService method Unban Positive Scenario.")
    void testMethodUnbanPositiveScenario() {
      userOne.setStatus(Status.BANNED);
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));
      when(userRepository.save(userOne)).thenReturn(userOne);

      doNothing().when(mailService).sendSimpleHtmlMailMessage4Line(anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString());

      String user = userService.unbanned(userOne.getEmail());

      assertEquals("User activated successfully", user);
    }

    @Test
    @DisplayName("Test UserService method Unban Negative Scenario. User with email not found.")
    void testMethodUnbanNegativeScenarioNotFound() {
      String notExistEmail = "sewewtnot@code.com";
      when(userRepository.findUserByEmail(notExistEmail)).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.banned(notExistEmail));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals(String.format("User with such email: (%s) not found", notExistEmail),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(notExistEmail);
      verify(userRepository, times(0)).save(any());
    }


    @Test
    @DisplayName("Test UserService method Unban Negative Scenario. User is already active.")
    void testMethodUnbanNegativeScenarioAlreadyActive() {
      userOne.setStatus(Status.ACTIVE);
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));

      String alreadyActive = userService.unbanned(userOne.getEmail());
      assertEquals("User is already active", alreadyActive);
//      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
//          () -> userService.banned(userOne.getEmail()));

//      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
//      assertEquals("User is already banned", errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(userOne.getEmail());
      verify(userRepository, times(0)).save(any());
    }
  }
}