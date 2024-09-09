package com.BookMyEvent.controller;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

    @GetMapping("/users")
private List<User> getUsers(){
  return userService.GetUsers();
}

}
