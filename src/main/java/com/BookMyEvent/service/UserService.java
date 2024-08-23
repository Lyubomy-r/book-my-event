package com.BookMyEvent.service;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;

import java.util.List;

public interface UserService {

   List<UserResponseDto> findAllUserProfiles();

   UserResponseDto findUserInfoById(String id);

   UserResponseDto findUserInfoByEmail(String userEmail);

   UserResponseDto save(User user);
}
