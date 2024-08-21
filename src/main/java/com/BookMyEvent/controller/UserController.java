package com.BookMyEvent.controller;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.UsersRepository;
import com.BookMyEvent.entity.Events;
import com.BookMyEvent.entity.Users;
import com.BookMyEvent.service.UserService;
import com.BookMyEvent.service.serviceImp.UserServiceImp;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping()
  public ResponseEntity<List<Users>> findAll (){
    return ResponseEntity.ok(userService.findAll());
  }
  @GetMapping("/{id}")
  public ResponseEntity<Users> findById (@PathParam("id") String id){
    return ResponseEntity.ok(userService.findById(id));
  }

  @PostMapping()
  public ResponseEntity<Users> save (@RequestBody Users user){
    return ResponseEntity.ok(userService.save(user));
  }


}
