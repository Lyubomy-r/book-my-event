package com.BookMyEvent.service;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;

import java.util.List;

public interface UserService {
   public List<User> GetUsers();

}
