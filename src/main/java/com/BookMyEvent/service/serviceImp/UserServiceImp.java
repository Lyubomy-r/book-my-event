package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public List<User> GetUsers(){
    return userRepository.findAll();
  }
}
