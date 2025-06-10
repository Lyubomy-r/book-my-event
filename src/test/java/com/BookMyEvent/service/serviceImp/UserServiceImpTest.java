package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.FundsRequestRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.*;
import com.BookMyEvent.entity.Enums.*;
import com.BookMyEvent.entity.dto.FundsStatus;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.DeletedUsersService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.OrderDetailsService;
import com.BookMyEvent.service.UserLikedEventService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class UserServiceImpTest {

  @Mock private UserRepository userRepository;
  @Mock private MailService mailService;
  @Mock private DeletedUsersService deletedUsersService;
  @Mock private UserMapper userMapper;
  @Mock private UserLikedEventService likedEventService;
  @Mock private OrderDetailsService orderDetailsService;
  @Mock private FundsRequestRepository fundsRequestRepository;

  @InjectMocks private UserServiceImp userService;

  private User userOne;
  private UserResponseDto userResponseDto;

  @BeforeEach
  void createUserOne() {

    userOne =
        User.builder()
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

    userResponseDto =
        UserResponseDto.builder()
            .id(userOne.getId().toHexString())
            .name(userOne.getName())
            .email(userOne.getEmail())
            .location(userOne.getLocation())
            .mailConfirmation(userOne.isMailConfirmation())
            .status(userOne.getStatus())
            .role(userOne.getRole())
            .creationDate(userOne.getCreationDate())
            .build();
  }

  @Test
  @DisplayName("Test UserService method FindAllUserProfiles")
  public void testFindAllUserProfiles() {
    //    when(userRepository.findAllUserProfiles()).thenReturn(List.of());

    Pageable pageable = PageRequest.of(0, 6);
    when(userRepository.findAll(pageable)).thenReturn(Page.empty());
    ;
    Page<UserResponseDto> resultEmptyList = userService.findAllUserProfiles(pageable);

    assertTrue(resultEmptyList.getContent().isEmpty());

    when(userRepository.findAll(pageable))
        .thenReturn(new PageImpl<>(List.of(userOne), pageable, 1));
    when(userMapper.toUserResponseDtoWithoutAvatarAndEvents(userOne)).thenReturn(userResponseDto);
    //    when(userRepository.findAllUserProfiles()).thenReturn(List.of(userResponseDto));

    Page<UserResponseDto> result = userService.findAllUserProfiles(pageable);
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.getContent().size()),
        () -> assertTrue(result.getContent().contains(userResponseDto)));

    verify(userRepository, times(2)).findAll(pageable);
  }

  @Nested
  @DisplayName("Test UserService method FindUserInfoById.")
  class FindUserInfoById {
    @Test
    @DisplayName("Test UserService method FindUserInfoById. Positive Scenario User is Find by id.")
    public void testFindUserInfoByIdPositiveScenarioFindUser() {
      when(userRepository.findUserInfoWithoutEventsById(userOne.getId()))
          .thenReturn(Optional.of(userOne));
      when(userMapper.toUserResponseDtoWithoutEvents(userOne)).thenReturn(userResponseDto);
      UserResponseDto responseDto = userService.findUserInfoById(userOne.getId().toHexString());
      assertAll(
          () -> assertEquals(userOne.getId().toHexString(), responseDto.getId()),
          () -> assertEquals(userOne.getEmail(), responseDto.getEmail()),
          () -> assertEquals(userOne.getRole(), responseDto.getRole()));

      verify(userRepository, times(1)).findUserInfoWithoutEventsById(userOne.getId());
    }

    @Test
    @DisplayName(
        "Test UserService method FindUserInfoById. Negative Scenario User id is null or empty.")
    public void testFindUserInfoByIdNegativeScenarioUserIdIsNull() {
      GeneralException errorIfUserIdNull =
          assertThrows(GeneralException.class, () -> userService.findUserInfoById(null));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserIdNull.getHttpStatus());
      assertEquals("User ID cannot be null or empty", errorIfUserIdNull.getMessage());

      verify(userRepository, times(0)).findUserInfoById(userOne.getId().toHexString());
    }

    @Test
    @DisplayName("Test UserService method FindUserInfoById. Negative Scenario User Not Found.")
    public void testFindUserInfoByIdNegativeScenarioUserNotFound() {
      when(userRepository.findUserInfoWithoutEventsById(userOne.getId()))
          .thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound =
          assertThrows(
              GeneralException.class,
              () -> userService.findUserInfoById(userOne.getId().toHexString()));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(
          String.format("User with ID [%s] not found.", userOne.getId().toHexString()),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserInfoWithoutEventsById(userOne.getId());
    }
  }

  @Nested
  @DisplayName("Test UserService method findUserInfoByEmail.")
  class FindUserInfoByEmail {
    @Test
    @DisplayName(
        "Test UserService method findUserInfoByEmail. Positive Scenario User is Find by Email.")
    public void testFindUserInfoByEmailPositiveScenarioFindUser() {
      when(userRepository.findUserInfoByEmail(userOne.getEmail()))
          .thenReturn(Optional.of(userResponseDto));

      UserResponseDto responseDto = userService.findUserInfoByEmail(userOne.getEmail());
      assertAll(
          () -> assertEquals(userOne.getId().toHexString(), responseDto.getId()),
          () -> assertEquals(userOne.getEmail(), responseDto.getEmail()),
          () -> assertEquals(userOne.getRole(), responseDto.getRole()));

      verify(userRepository, times(1)).findUserInfoByEmail(userOne.getEmail());
    }

    @Test
    @DisplayName(
        "Test UserService method findUserInfoByEmail. Negative Scenario User Email is null or empty.")
    public void testFindUserInfoByEmailNegativeScenarioUserEmailIsNull() {

      GeneralException errorIfUserIdNull =
          assertThrows(GeneralException.class, () -> userService.findUserInfoByEmail(null));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserIdNull.getHttpStatus());
      assertEquals("User Email cannot be null or empty", errorIfUserIdNull.getMessage());

      verify(userRepository, times(0)).findUserInfoByEmail(userOne.getEmail());
    }

    @Test
    @DisplayName("Test UserService method findUserInfoByEmail. Negative Scenario User Not Found.")
    public void testFindUserInfoByEmailNegativeScenarioUserNotFound() {
      when(userRepository.findUserInfoByEmail(userOne.getEmail())).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound =
          assertThrows(
              GeneralException.class, () -> userService.findUserInfoByEmail(userOne.getEmail()));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(
          String.format("User with Email [%s] not found.", userOne.getEmail()),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserInfoByEmail(userOne.getEmail());
    }
  }

  //
  //  @Test
  //  void save() {
  //  }
  //
  //  @Nested
  //  @DisplayName("Test UserService method updateFieldsFromAdmin")
  //  class UpdateFieldsFromAdmin {
  //    @Test
  //    @DisplayName("Test UserService method updateFieldsFromAdmin. Positive Scenario User is
  // Updated.")
  //    void testUpdateFieldsFromAdminPositiveScenarioUpdatedUser() {
  //
  //    }
  //  }
  //
  //  @Test
  //  void delete() {
  //  }

  @Nested
  @DisplayName("Test UserService method DeleteFromAdmin")
  class DeleteFromAdmin {
    @Test
    @DisplayName("Test UserService method deleteFromAdmin. Positive Scenario User was Deleted.")
    void testDeleteFromAdminPositiveScenarioUserDeleted() {
      when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
      doNothing().when(deletedUsersService).addUserToDeletedList(userOne.getEmail());
      doNothing().when(userRepository).delete(userOne);
      doNothing().when(likedEventService).deleteByUserId(userOne.getId().toHexString());

      String responseMessage = userService.deleteFromAdmin(userOne.getId().toHexString());
      assertAll(
          () -> assertFalse(responseMessage.isEmpty()),
          () -> assertEquals("User was deleted successfully.", responseMessage));

      verify(userRepository, times(1)).findById(userOne.getId());
      verify(deletedUsersService, times(1)).addUserToDeletedList(userOne.getEmail());
      verify(userRepository, times(1)).delete(userOne);
      verify(likedEventService, times(1)).deleteByUserId(userOne.getId().toHexString());
    }

    @Test
    @DisplayName(
        "Test UserService method deleteFromAdmin. Negative Scenario User id is null or empty.")
    public void testDeleteFromAdminNegativeScenarioUserIdIsNull() {

      GeneralException errorIfUserIdNull =
          assertThrows(GeneralException.class, () -> userService.deleteFromAdmin(null));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserIdNull.getHttpStatus());
      assertEquals(
          String.format("User ID cannot be null or empty. %s ", null),
          errorIfUserIdNull.getMessage());

      verify(userRepository, times(0)).findUserInfoByEmail(userOne.getEmail());
    }

    @Test
    @DisplayName("Test UserService method deleteFromAdmin. Negative Scenario User Not Found.")
    public void testDeleteFromAdminNegativeScenarioUserNotFound() {
      when(userRepository.findById(userOne.getId())).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound =
          assertThrows(
              GeneralException.class,
              () -> userService.deleteFromAdmin(userOne.getId().toHexString()));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(
          String.format("User with ID [%s] not found.", userOne.getId().toHexString()),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findById(userOne.getId());
    }
  }

  @Nested
  @DisplayName("Test UserService method Banned")
  class UserBannedTests {
    @Test
    @DisplayName("Test UserService method Banned Positive Scenario")
    void testMethodBannedPositiveScenario() {
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));
      when(userRepository.save(userOne)).thenReturn(userOne);
      doNothing()
          .when(mailService)
          .sendSimpleHtmlMailMessage4Line(
              anyString(),
              anyString(),
              anyString(),
              anyString(),
              anyString(),
              anyString(),
              anyString());
      String user = userService.banned(userOne.getEmail());

      assertEquals("User status updated to 'BANNED'", user);

      verify(userRepository, times(1)).findUserByEmail(userOne.getEmail());
      verify(userRepository, times(1)).save(userOne);
    }

    @Test
    @DisplayName("Test UserService method Banned Negative Scenario. User with email not found.")
    void testMethodBannedNegativeScenarioNotFound() {
      String notExistEmail = "sewewtnot@code.com";
      when(userRepository.findUserByEmail(notExistEmail)).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound =
          assertThrows(GeneralException.class, () -> userService.banned(notExistEmail));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(
          String.format("User with such email: (%s) not found", notExistEmail),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(notExistEmail);
      verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Test UserService method Banned Negative Scenario. User is already banned")
    void testMethodBannedNegativeScenarioAlreadyBanned() {
      userOne.setStatus(Status.BANNED);
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));

      GeneralException errorIfUserNotFound =
          assertThrows(GeneralException.class, () -> userService.banned(userOne.getEmail()));

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

      doNothing()
          .when(mailService)
          .sendSimpleHtmlMailMessage4Line(
              anyString(),
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

      GeneralException errorIfUserNotFound =
          assertThrows(GeneralException.class, () -> userService.banned(notExistEmail));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(
          String.format("User with such email: (%s) not found", notExistEmail),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(notExistEmail);
      verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Test UserService method Unban Negative Scenario. User is already active.")
    void testMethodUnbanNegativeScenarioAlreadyActive() {
      userOne.setStatus(Status.ACTIVE);
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));

      //      String alreadyActive = userService.unbanned(userOne.getEmail());
      //      assertEquals("User is already active", alreadyActive);
      GeneralException errorIfUserNotFound =
          assertThrows(GeneralException.class, () -> userService.unbanned(userOne.getEmail()));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals("User is already active", errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(userOne.getEmail());
      verify(userRepository, times(0)).save(any());
    }
  }

  @Test
  @DisplayName(
          "Test UserService method GetUserTotalProfit Positive Scenario. Return User total profit.")
  void testMethodGetUserTotalProfit() {
    LocalTime localTime = LocalTime.now();
    Event eventCompleted = new Event();
    eventCompleted.setId(new ObjectId("66c648b600179737a3d5c235"));
    eventCompleted.setTitle("Test Event");
    eventCompleted.setDescription("Test Description");
    eventCompleted.setEventType(EventType.SPORTS_EVENTS);
    eventCompleted.setEventCategory(EventCategory.TOP_EVENTS);
    eventCompleted.setEventStatus(EventStatus.APPROVED);
    eventCompleted.setEventFormat(EventFormat.OFFLINE);
    eventCompleted.setTicketPrice(800L);
    eventCompleted.setAvailableTickets(999);
    eventCompleted.setNumberOfTickets(1000);
    eventCompleted.setSoldTickets(1);
    eventCompleted.setProfit(BigDecimal.valueOf(800L));
    eventCompleted.setOrganizers(
            User.builder()
                    .id(userOne.getId())
                    .email(userOne.getEmail())
                    .createdEvents(List.of(eventCompleted))
                    .build());
    eventCompleted.setDate(
            new DateDetails(
                    LocalDate.of(LocalDate.now().plusYears(1).getYear(), 10, 21).toString(),
                    localTime.toString(),
                    localTime.plusHours(2L).toString()));
    eventCompleted.setCompleted(true);
    Event eventNotCompleted = new Event();
    eventNotCompleted.setId(new ObjectId("66c648b600179737a3d5c236"));
    eventNotCompleted.setTitle("Test Event");
    eventNotCompleted.setDescription("Test Description");
    eventNotCompleted.setEventType(EventType.SPORTS_EVENTS);
    eventNotCompleted.setEventCategory(EventCategory.TOP_EVENTS);
    eventNotCompleted.setEventStatus(EventStatus.APPROVED);
    eventNotCompleted.setEventFormat(EventFormat.OFFLINE);
    eventNotCompleted.setTicketPrice(700L);
    eventNotCompleted.setAvailableTickets(999);
    eventNotCompleted.setNumberOfTickets(1000);
    eventNotCompleted.setSoldTickets(1);
    eventNotCompleted.setProfit(BigDecimal.valueOf(700L));
    eventNotCompleted.setOrganizers(
            User.builder()
                    .id(userOne.getId())
                    .email(userOne.getEmail())
                    .createdEvents(List.of(eventNotCompleted))
                    .build());
    eventNotCompleted.setDate(
            new DateDetails(
                    LocalDate.of(LocalDate.now().plusYears(1).getYear(), 10, 21).toString(),
                    localTime.toString(),
                    localTime.plusHours(2L).toString()));
    eventNotCompleted.setCompleted(true);
    FundsRequest fundsRequest =
            FundsRequest.builder()
                    .id("66c648b600179737a3d5c654")
                    .eventIds(List.of(eventCompleted.getId().toHexString()))
                    .creationDate(Instant.now())
                    .status(FundsStatus.APPROVED)
                    .amount(BigDecimal.valueOf(800L))
                    .userId(userOne.getId().toHexString())
                    .build();
    userOne.setCreatedEvents(List.of(eventCompleted, eventNotCompleted));

    when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
    when(fundsRequestRepository.findByUserIdAndStatusIn(
            userOne.getId().toHexString(), List.of(FundsStatus.PENDING, FundsStatus.COMPLETED)))
            .thenReturn(List.of(fundsRequest));
    when(orderDetailsService.calculateTotalUserProfit(List.of(eventNotCompleted)))
            .thenReturn(BigDecimal.valueOf(700L));
    when(orderDetailsService.calculateTotalUserProfit(List.of(eventCompleted)))
            .thenReturn(BigDecimal.valueOf(800L));

    Map<String, BigDecimal> totalPrositMap =
            userService.getUserTotalProfit(userOne.getId().toHexString());

    assertAll(
            () -> assertFalse(totalPrositMap.isEmpty()),
            () -> assertEquals(BigDecimal.valueOf(700L), totalPrositMap.get("totalProfit")),
            () -> assertEquals(BigDecimal.valueOf(800L), totalPrositMap.get("receivedTotalProfit"))
    );

    verify(userRepository, times(1)).findById(userOne.getId());
    verify(fundsRequestRepository, times(1))
            .findByUserIdAndStatusIn(
                    userOne.getId().toHexString(), List.of(FundsStatus.PENDING, FundsStatus.COMPLETED));
    verify(orderDetailsService, times(2)).calculateTotalUserProfit(anyList());
  }
}
