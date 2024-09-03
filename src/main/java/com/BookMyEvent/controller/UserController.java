package com.BookMyEvent.controller;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.service.SingInServiceInterface;
import com.BookMyEvent.service.UserService;
import com.BookMyEvent.service.serviceImp.SingInService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
  private SingInService service;
  private UserService userService;

//  @PostMapping("/UserRegistration")
//  public String UserRegistration(@RequestBody User user) {
//    return  service.UserRegistration(user);
//  }


}
