package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

//  @GetMapping()
//  @PreAuthorize("hasRole('ADMIN')")
//  public ResponseEntity<List<UserResponseDto>> findAllUserProfiles() {
//    List<UserResponseDto> userList = userService.findAllUserProfiles();
//    log.info("UserController::findAllUserProfiles - /users - Return list of all users.");
//    return ResponseEntity.ok(userList);
//  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponseDto> findUserInfoById(@PathVariable("userId") String userId) {
    UserResponseDto userResponse = userService.findUserInfoById(userId);
    log.info("UserController::findUserInfoById - /users/{userId} - Return User Info email: {} .", userResponse.getEmail());
    return ResponseEntity.ok(userResponse);
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<UserResponseDto> updateFields(@PathVariable("userId") String userId,
                                                               @RequestBody UserUpdateDto userUpdateDto) {
    UserResponseDto userResponse = userService.updateFieldsFromAdmin(userId, userUpdateDto);
    log.info("UserController::updateFieldsFromAdmin - /users/{userId} - Return updated User Info email: {} .", userResponse.getEmail());
    return ResponseEntity.ok(userResponse);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<AppResponse> delete(@PathVariable("userId") String userId) {
    AppResponse response = new AppResponse(
        HttpStatus.OK.value(), userService.delete(userId));
    log.info("UserController::delete - /users/{userId} - Return deletion message.");
    return ResponseEntity.ok(response);
  }
}
