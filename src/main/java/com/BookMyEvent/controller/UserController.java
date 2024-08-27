package com.BookMyEvent.controller;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;

  @GetMapping()
  public ResponseEntity<List<UserResponseDto>> findAllUserProfiles() {
    return ResponseEntity.ok(userService.findAllUserProfiles());
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponseDto> findUserInfoById(@PathVariable("userId") String userId) {
    return ResponseEntity.ok(userService.findUserInfoById(userId));
  }

  @PostMapping()
  public ResponseEntity<UserResponseDto> save(@RequestBody User user) {
    return ResponseEntity.ok(userService.save(user));
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<UserResponseDto> updateFieldsFromAdmin(@PathVariable("userId") String userId,
                                                               @RequestBody UserUpdateDto userUpdateDto) {
    return ResponseEntity.ok(userService.updateFieldsFromAdmin(userId, userUpdateDto));
  }

  @DeleteMapping ("/{userId}")
  public ResponseEntity <String> delete(@PathVariable("userId") String userId){
    return ResponseEntity.ok(userService.delete(userId));
  }

}
