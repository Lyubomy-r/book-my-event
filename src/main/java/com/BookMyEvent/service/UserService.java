package com.BookMyEvent.service;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

   List<UserResponseDto> findAllUserProfiles();

   UserResponseDto findUserInfoById(String id);

   UserResponseDto findUserInfoByEmail(String userEmail);

   UserResponseDto save(User user);

   UserResponseDto updateFieldsFromAdmin(String userId, UserUpdateDto userUpdateDto);

   String delete(String userId);

//   Page<UserResponseDto> getUserPage(int size, int page);
   List<UserResponseDto> getUser();

   String banned(String email);

   String unbanned(String email);

}
