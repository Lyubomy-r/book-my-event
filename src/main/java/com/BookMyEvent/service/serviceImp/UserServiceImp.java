package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public static final String NOT_FOUND_MESSAGE_ID = "User with ID [%s] not found.";
  public static final String NOT_FOUND_MESSAGE_EMAIL = "User with Email [%s] not found.";

  @Override
  public List<UserResponseDto> findAllUserProfiles() {
    List<UserResponseDto> userList = userRepository.findAllUserProfiles();
    log.info("UserServiceImp::findAllUserProfiles. Return all existing users.");
    return userList;
  }

  @Override
  public UserResponseDto findUserInfoById(String userId) {
    if (userId == null || userId.isEmpty()) {
      log.warn("UserServiceImp::findUserInfoById. Return error message.");
      throw new GeneralException("User ID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    Optional<UserResponseDto> user = userRepository.findUserInfoById(userId);
    if (user.isPresent()) {
      log.info("UserServiceImp::findUserInfoById. Return user by ID: {}.", userId);

      return user.get();
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
      throw new GeneralException(String.format(NOT_FOUND_MESSAGE_EMAIL, userEmail), HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public UserResponseDto save(User user) {
    if(userRepository.existsByEmail(user.getEmail())){
      log.warn("UserServiceImp::save. Return error message.");
      throw new GeneralException(String.format("User with email <%s> already exists", user.getEmail()), HttpStatus.BAD_REQUEST);
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
  public UserResponseDto updateFieldsFromAdmin(String userId, UserUpdateDto userUpdateDto) {
    if (userUpdateDto == null || (userId == null || userId.isEmpty())) {
      log.warn("UserServiceImp::updateFields. Return error message.");
      throw new GeneralException("Can't make changes fields is null or empty.", HttpStatus.BAD_REQUEST);
    }
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("UserServiceImp::updateFields. Return error message.");
          return new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
        });
    userMapper.mapUserUpdateToUser(userUpdateDto, user);
    User updateUser = userRepository.save(user);

    return userMapper.toUserResponseDto(updateUser);
  }

  public String delete(String userId) {
    if (userId == null || userId.isEmpty()) {
      log.warn("UserServiceImp::delete. Return error message.");
      throw new GeneralException(String.format("User ID cannot be null or empty. %s ", userId), HttpStatus.BAD_REQUEST);
    }
    if (userRepository.existsById(userId)) {
      log.info("UserServiceImp::delete. Deleted user by ID: {}.", userId);
      userRepository.deleteById(userId);

      return "User was deleted successfully.";
    } else {
      log.warn("UserServiceImp::delete. Return error message.");
      throw new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public Page<User> getPage(int size, int page) {
    log.info("Fetching page of users with page number: {} and size: {}", page, size);

    if (size <= 0) {
      log.warn("Invalid page size: {}. Setting to default size: 10", size);
      size = 10;
    }
    if (page < 0) {
      log.warn("Invalid page number: {}. Setting to default page: 0", page);
      page = 0;
    }

    Pageable pageable = PageRequest.of(page, size);
    Page<User> userPage = userRepository.findAll(pageable);
    log.info("Fetched {} users from page {}", userPage.getNumberOfElements(), page);

    return userPage;
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
        log.info("User status updated to 'BANNED' for user: {}", user.getEmail());
        return "User status updated to 'BANNED'";
      } else {
        log.warn("User with email: {} is already banned.", email);
        throw new GeneralException("User is already banned", HttpStatus.BAD_REQUEST);
      }
    } else {
      log.warn("No user found with email: {}", email);
      throw new GeneralException(
              String.format("User with such email: (%s) not found", email),
              HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public String unban(String email) {
    log.info("Attempting to activate user with email: {}", email);

    var userOptional = userRepository.findUserByEmail(email);
    if (userOptional.isPresent()) {
      var user = userOptional.get();
      log.info("User found: {} with current status: {}", user.getEmail(), user.getStatus());

      if (!user.getStatus().equals(Status.ACTIVE)) {
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        log.info("User status successfully updated to 'ACTIVE' for user: {}", user.getEmail());
        return "User activated successfully";
      } else {
        log.warn("User with email: {} is already active.", email);
        return "User is already active";
      }
    } else {
      log.warn("No user found with email: {}", email);
    }

    log.info("Activation operation for user with email {} completed with result: 'User not found'", email);
    return "User not found";
  }
}
