package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.FundsRequestRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.FundsRequest;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.FundsStatus;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.EventMapper;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {

  private final UserRepository userRepository;
  private final DeletedUsersService deletedUsersService;
  private final UserMapper userMapper;
  private final MailService mailService;
  private final CloudinaryService mediaService;
  private final PasswordEncoder passwordEncoder;
  private final UserLikedEventService likedEventService;
  private final OrderDetailsService orderDetailsService;
  private final FundsRequestRepository fundsRequestRepository;

  @Value("${front.url}")
  private String frontUrl;

  @Value("${company.phone}")
  private String companyPhone;

  public static final String NOT_FOUND_MESSAGE_ID = "User with ID [%s] not found.";
  public static final String NOT_FOUND_MESSAGE_EMAIL = "User with Email [%s] not found.";
  private final String className = this.getClass().getSimpleName();

  @Override
  public Page<UserResponseDto> findAllUserProfiles(Pageable pageable) {
    //    List<UserResponseDto> userList = userRepository.findAllUserProfiles();
    Page<User> userPage = userRepository.findAll(pageable);
    log.info("{}::findAllUserProfiles. Return all existing users.", className);
    return userPage.map(userMapper::toUserResponseDtoWithoutAvatarAndEvents);
    //    return userL.stream()
    //        .map(userMapper::toUserResponseDtoWithoutAvatarAndEvents)
    //        .toList();
  }

  @Override
  public UserResponseDto findUserInfoById(String userId) {
    if (userId == null || userId.isEmpty()) {
      log.warn("{}::findUserInfoById. Return error message.", className);
      throw new GeneralException("User ID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    //    Optional<UserResponseDto> user = userRepository.findUserInfoById(userId);
    Optional<User> user = userRepository.findUserInfoWithoutEventsById(new ObjectId(userId));
    if (user.isPresent()) {
      log.info("UserServiceImp::findUserInfoById. Return user by ID: {}.", userId);

      return userMapper.toUserResponseDtoWithoutEvents(user.get());
    } else {
      log.warn("UserServiceImp::findUserInfoById. Return error message.");
      throw new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public UserResponseDto findUserInfoByEmail(String userEmail) {
    if (userEmail == null || userEmail.isEmpty()) {
      log.warn("UserServiceImp::findUserInfoByEmail. Return error message.");
      throw new GeneralException("User Email cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    Optional<UserResponseDto> user = userRepository.findUserInfoByEmail(userEmail);
    if (user.isPresent()) {
      log.info("UserServiceImp::findUserInfoByEmail. Return user by Email: {}.", userEmail);

      return user.get();
    } else {
      log.warn("UserServiceImp::findUserInfoByEmail. Return error message.");
      throw new GeneralException(
          String.format(NOT_FOUND_MESSAGE_EMAIL, userEmail), HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public User findUserById(String userId) {
    if (userId == null || userId.isEmpty()) {
      log.warn("{}::findUserInfoById. Return error message.", className);
      throw new GeneralException("User ID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    User user =
        userRepository
            .findById(new ObjectId(userId))
            .orElseThrow(
                () -> {
                  log.warn("UserServiceImp::findUserInfoById. Return error message.");
                  return new GeneralException(
                      String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
                });
    log.info("UserServiceImp::findUserInfoById. Return user by ID: {}.", userId);

    return user;
  }

  //  @Override
  //  public  Page<EventResponseDto> findUserCreatedEvents(String userId, Pageable pageable) {
  //    if (userId == null || userId.isEmpty()) {
  //      log.warn("{}::findUserInfoById. Return error message.", className);
  //      throw new GeneralException("User ID cannot be null or empty", HttpStatus.BAD_REQUEST);
  //    }
  //
  //    log.info("UserServiceImp::findUserInfoById. Return user by ID: {}.", userId);
  //    Page<EventResponseDto> list = eventService.getByOrganizersId(userId, pageable);
  //    return list;
  //  }

  @Override
  public UserResponseDto save(User user) {
    if (userRepository.existsByEmail(user.getEmail())) {
      log.warn("UserServiceImp::save. Return error message.");
      throw new GeneralException(
          String.format("User with email <%s> already exists", user.getEmail()),
          HttpStatus.BAD_REQUEST);
    }
    try {
      User newUser = userRepository.save(user);
      log.info("UserServiceImp::save. Return saved user by id: {}.", newUser.getId());

      return userMapper.toUserResponseDto(newUser);
    } catch (Exception e) {
      log.warn("UserServiceImp::save. Return error message : {}.", e.getMessage());
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public UserResponseDto updateUserFields(String userId, UserUpdateDto userUpdateDto) {
    if (userUpdateDto == null || (userId == null || userId.isEmpty())) {
      log.warn("UserServiceImp::updateFields. Return error message.");
      throw new GeneralException(
          "Can't make changes fields is null or empty.", HttpStatus.BAD_REQUEST);
    }
    User user =
        userRepository
            .findById(new ObjectId(userId))
            .orElseThrow(
                () -> {
                  log.warn("UserServiceImp::updateFields. Return error message.");
                  return new GeneralException(
                      String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
                });
    //    userMapper.mapUserUpdateToUser(userUpdateDto, user);
    //    checkAndAddPasswordIfExist(userUpdateDto, user);

    User mappedUser = userMapper.mapUserUpdateToUser(userUpdateDto, user);
    mappedUser.setPassword(checkAndAddPasswordIfExist(userUpdateDto, user));
    User updateUser = userRepository.save(mappedUser);
    log.info(
        "!updateUser.getPassword().equals(user.getPassword()) {}",
        !updateUser.getPassword().equals(user.getPassword()));
    if (!updateUser.getPassword().equals(user.getPassword())) {
      mailService.sendSimpleHtmlMailMessage6Line(
          updateUser.getEmail(),
          "Твій пароль успішно оновлено BookMyEvent",
          "Привіт!",
          "Твій пароль було успішно оновлено.",
          "Тепер ти можете увійти до свого облікового запису за допомогою нового пароля: "
              + frontUrl,
          "Якщо ти не запитував зміну пароля, будь ласка, зверніться до нашої служби підтримки.",
          "",
          "",
          "");
    }
    return userMapper.toUserResponseDto(updateUser);
  }

  //  @Override
  //  public UserResponseDto updateUserFields(String userId, UserUpdateDto userUpdateDto) {
  //    if (userUpdateDto == null || (userId == null || userId.isEmpty())) {
  //      log.warn("UserServiceImp::updateFields. Return error message.");
  //      throw new GeneralException("Can't make changes fields is null or empty.",
  // HttpStatus.BAD_REQUEST);
  //    }
  //    User user = userRepository.findById(new ObjectId(userId))
  //        .orElseThrow(() -> {
  //          log.warn("UserServiceImp::updateFields. Return error message.");
  //          return new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId),
  // HttpStatus.NOT_FOUND);
  //        });
  //    userMapper.mapUserUpdateToUser(userUpdateDto, user);
  //    User updateUser = userRepository.save(user);
  //
  //    return userMapper.toUserResponseDto(updateUser);
  //  }

  @Override
  public UserResponseDto updateUserAvatar(String userId, MultipartFile userAvatar) {
    User user =
        userRepository
            .findById(new ObjectId(userId))
            .orElseThrow(
                () -> {
                  log.warn("UserServiceImp::updateFields. Return error message.");
                  return new GeneralException(
                      String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
                });
    String imageName = user.getId().toHexString() + "/" + userAvatar.getOriginalFilename();
    Image newImage = mediaService.savedUserImage(userAvatar, user.getId().toHexString());
    newImage.setName(imageName);
    user.linkImageWithUser(newImage);
    User updateUser = userRepository.save(user);

    return userMapper.toUserResponseDto(updateUser);
  }

  @Override
  public String delete(String userId) {

    if (userId == null || userId.isEmpty()) {
      log.warn("UserServiceImp::delete. Return error message.");
      throw new GeneralException(
          String.format("User ID cannot be null or empty. %s ", userId), HttpStatus.BAD_REQUEST);
    }
    User user =
        userRepository
            .findById(new ObjectId(userId))
            .orElseThrow(
                () -> {
                  log.warn("UserServiceImp::updateFields. Return error message.");
                  return new GeneralException(
                      String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
                });
    log.info("UserServiceImp::delete. Deleted user by ID: {}.", userId);
    if (user.getAvatarImage() != null) {
      mediaService.deleteUserImg(user.getAvatarImage());
    }
    userRepository.delete(user);
    likedEventService.deleteByUserId(user.getId().toHexString());
    return "User was deleted successfully.";
  }

  @Override
  public String deleteUserList(List<String> userId) {

//    if (userId == null || userId.isEmpty()) {
//      log.warn("UserServiceImp::delete. Return error message.");
//      throw new GeneralException(
//              String.format("User ID cannot be null or empty. %s ", userId), HttpStatus.BAD_REQUEST);
//    }
    List<User> users =userId.stream()
            .map(srtId-> userRepository
                    .findById(new ObjectId(srtId)).get())
            .toList();
//            userRepository
//                    .findById(new ObjectId(userId))
//                    .orElseThrow(
//                            () -> {
//                              log.warn("UserServiceImp::updateFields. Return error message.");
//                              return new GeneralException(
//                                      String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
//                            });
    log.info("UserServiceImp::deleteUserList. Deleted all user from list by ID.");
    users.forEach(user-> mediaService.deleteUserImg(user.getAvatarImage()));
    userRepository.deleteAll(users);
    users.forEach(user->  likedEventService.deleteByUserId(user.getId().toHexString()));
    return "Users was deleted successfully.";
  }

  @Override
  public String deleteFromAdmin(String userId) {
    if (userId == null || userId.isEmpty()) {
      log.warn("UserServiceImp::deleteFromAdmin. Return error message.");
      throw new GeneralException(
          String.format("User ID cannot be null or empty. %s ", userId), HttpStatus.BAD_REQUEST);
    }
    User user =
        userRepository
            .findById(new ObjectId(userId))
            .orElseThrow(
                () -> {
                  log.warn("UserServiceImp::deleteFromAdmin. Return error message.");
                  return new GeneralException(
                      String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
                });
    log.info("UserServiceImp::deleteFromAdmin. Deleted user by ID: {}.", userId);

    deletedUsersService.addUserToDeletedList(user.getEmail());
    if (user.getAvatarImage() != null) {
      mediaService.deleteUserImg(user.getAvatarImage());
    }
    userRepository.delete(user);
    likedEventService.deleteByUserId(user.getId().toHexString());
    return "User was deleted successfully.";
  }

  @Override
  public String banned(String email) {
    log.info("Attempting to ban user with email: {}", email);

    var userOptional = userRepository.findUserByEmail(email);
    if (userOptional.isPresent()) {
      var user = userOptional.get();
      log.info("User found: {} with current status: {}", user.getEmail(), user.getStatus());

      if (!user.getStatus().equals(Status.BANNED)) {
        user.setStatus(Status.BANNED);
        userRepository.save(user);
        //        mailService.blockingMessage(user.getEmail());
        mailService.sendSimpleHtmlMailMessage4Line(
            user.getEmail(),
            "Твій акаунт тимчасово заблоковано – що робити далі?",
            "",
            "Твій акаунт заблоковано, доступ обмежено у зв’язку з недотриманням правил платформи.",
            "Якщо у тебе є питання, зателефонуй на нашу гарячу лінію.",
            "\uD83D\uDCF2 " + companyPhone,
            "");
        log.info("User status updated to 'BANNED' for user: {}", user.getEmail());
        return "User status updated to 'BANNED'";
      } else {
        log.warn("User with email: {} is already banned.", email);
        throw new GeneralException("User is already banned", HttpStatus.BAD_REQUEST);
      }
    } else {
      log.warn("No user found with email: {}", email);
      throw new GeneralException(
          String.format("User with such email: (%s) not found", email), HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public UserResponseDto deleteUserAvatar(String userId) {
    User user =
        userRepository
            .findById(new ObjectId(userId))
            .orElseThrow(
                () -> {
                  log.warn("UserServiceImp::deleteUserAvatar. Return error message.");
                  return new GeneralException(
                      String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
                });
    mediaService.deleteUserImg(user.getAvatarImage());

    user.setAvatarImage(null);
    User updateUser = userRepository.save(user);

    return userMapper.toUserResponseDto(updateUser);
  }

  @Override
  public String unbanned(String email) {
    log.info("Attempting to activate user with email: {}", email);

    var userOptional = userRepository.findUserByEmail(email);
    if (userOptional.isPresent()) {
      var user = userOptional.get();
      log.info("User found: {} with current status: {}", user.getEmail(), user.getStatus());

      if (!user.getStatus().equals(Status.ACTIVE)) {
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        //        mailService.unblockingMessage(user.getEmail());
        mailService.sendSimpleHtmlMailMessage4Line(
            user.getEmail(),
            "Твій акаунт знову активний – ласкаво просимо назад!",
            "Вітаємо!",
            "Твій акаунт розблоковано, і ти знову можете користуватися всіма можливостями нашого сайту. Насолоджуйся!",
            "",
            "",
            "");
        log.info("User status successfully updated to 'ACTIVE' for user: {}", user.getEmail());

        return "User activated successfully";
      } else {
        log.warn("User with email: {} is already active.", email);
        throw new GeneralException("User is already active", HttpStatus.BAD_REQUEST);
        //        return "User is already active";
      }
    } else {
      log.warn("No user found with email: {}", email);
      throw new GeneralException(
          String.format("User with such email: (%s) not found", email), HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public Map<String, BigDecimal> getUserTotalProfit(String userId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    User existUser = findUserById(userId);
    Predicate<Event> predicateFilterApproved =
        event -> event.getEventStatus().equals(EventStatus.APPROVED);
    List<Event> eventListApproved =
        existUser.getCreatedEvents().stream()
            .filter(predicateFilterApproved.and(Event::isCompleted))
            .toList();
    List<FundsRequest> fundsRequests =
        fundsRequestRepository.findByUserIdAndStatusIn(
            userId, List.of(FundsStatus.PENDING, FundsStatus.COMPLETED));
    Set<String> alreadyFundsRequestsEventId =
        fundsRequests.stream()
            .flatMap(funds -> funds.getEventIds().stream())
            .collect(Collectors.toSet());
    //    List<Event> newEventsToFundsRequest =
    //            eventListApproved.stream()
    //                    .filter(event ->
    // !alreadyFundsRequestsEventId.contains(event.getId().toHexString()))
    //                    .toList();
    Map<Boolean, List<Event>> booleanListMap =
        eventListApproved.stream()
            .collect(
                Collectors.partitioningBy(
                    event -> alreadyFundsRequestsEventId.contains(event.getId().toHexString()),
                    Collectors.toList()));
    BigDecimal totalProfitReceived =
        orderDetailsService.calculateTotalUserProfit(booleanListMap.get(true));
    if (booleanListMap.get(false).isEmpty()) {
      log.info(
          "{}::{} - find  user totalProfit : {} - receivedTotalProfit : {}.",
          className,
          methodName,
          BigDecimal.ZERO,
          totalProfitReceived);
      return Map.of("totalProfit", BigDecimal.ZERO, "receivedTotalProfit", totalProfitReceived);
    }
    BigDecimal totalProfitNotReceived =
        orderDetailsService.calculateTotalUserProfit(booleanListMap.get(false));
    log.info(
        "{}::{} - find  user totalProfit : {} - receivedTotalProfit : {}.",
        className,
        methodName,
        totalProfitNotReceived,
        totalProfitReceived);

    return Map.of(
        "totalProfit", totalProfitNotReceived, "receivedTotalProfit", totalProfitReceived);
  }

  @Override
  public Optional<User> findByGoogleId(String googleId) {
      return userRepository.findByGoogleId(googleId);
  }

  @Override
  public Optional<User> findByEmail(String googleId) {
      return userRepository.findByEmail(googleId);
  }

  @Override
  public Optional<User> findById(String userId) {
    return userRepository.findById(new ObjectId(userId));
  }

  @Override
  public User saveGoogleUser(User user) {
    log.info("{}::saveGoogleUser. Start method", className);
    if (deletedUsersService.emailExist(user.getEmail())) {
      log.warn("{}::saveGoogleUser. Return error message: Email is not longer accessible", className);
      throw new GeneralException(String.format("The email (%s) has been deleted and is no longer accessible.", user.getEmail()),
              HttpStatus.FORBIDDEN);
    }
    var existGoogleUser = userRepository.findUserByEmail(user.getEmail());
    if (existGoogleUser.isPresent()) {

      return existGoogleUser.get();
    } else {


//      var hashedPassword = passwordEncoder.encode(userData.getPassword());
//      userData.setPassword(hashedPassword);
//      User newUser = userMapper.toUserFromUserSaveDto(userData);
      LocalDateTime timeCreate = LocalDateTime.now();
      ZonedDateTime kyivTime = ZonedDateTime.now(ZoneId.of("Europe/Kiev"));
      log.info("timeCreate "+timeCreate);
      log.info("kievTime "+kyivTime);
      user.setCreationDate(timeCreate);
      user.setMailConfirmation(false);
      user.setRole(Role.VISITOR);
      user.setStatus(Status.ACTIVE);
      user.setMailConfirmation(Boolean.TRUE);
      User newUser = userRepository.save(user);

//      mailService.sendHtmlEmailAfterRegistration(userData.getEmail());
//      mailService.deleteOldEmails(userData.getEmail());
      log.info("{}::saveGoogleUser. Return newUser .", className);
      return newUser;
    }
  }


  public String checkAndAddPasswordIfExist(UserUpdateDto userUpdateDto, User user) {
    if (userUpdateDto.getPassword() != null
        && !userUpdateDto.getPassword().isEmpty()
        && !passwordEncoder.matches(userUpdateDto.getPassword(), user.getPassword())) {
      return passwordEncoder.encode(userUpdateDto.getPassword());
    } else {
      return user.getPassword();
    }
  }
}
