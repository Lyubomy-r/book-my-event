package com.BookMyEvent.service;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserService {

   Page<UserResponseDto> findAllUserProfiles(Pageable pageable);

   UserResponseDto findUserInfoById(String id);

   UserResponseDto findUserInfoByEmail(String userEmail);

   User findUserById(String userId);

//   Page<EventResponseDto> findUserCreatedEvents(String userId, Pageable pageable);

   UserResponseDto save(User user);

   UserResponseDto updateUserFields(String userId, UserUpdateDto userUpdateDto);
//   UserResponseDto updateUserFields(String userId, UserUpdateDto userUpdateDto);

   UserResponseDto updateUserAvatar(String userId,  MultipartFile userAvatar);

   String delete(String userId);

   String deleteFromAdmin(String userId);

   UserResponseDto deleteUserAvatar(String userId);

   String banned(String email);

   String unbanned(String email);

   Map<String, BigDecimal> getUserTotalProfit(String userId);

}
