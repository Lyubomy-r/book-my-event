package com.BookMyEvent.service;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;

import java.util.List;

public interface UserService {

   List<UserResponseDto> findAllUserProfiles();

   UserResponseDto findUserInfoById(String id);

   UserResponseDto findUserInfoByEmail(String userEmail);

   UserResponseDto save(User user);

   UserResponseDto updateFieldsFromAdmin(String userId, UserUpdateDto userUpdateDto);

   String delete(String userId);

   String banned(String email);

   String unbanned(String email);

}
