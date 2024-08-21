package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.UsersRepository;
import com.BookMyEvent.entity.Users;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

  private final UsersRepository usersRepository;

  @Override
  public List<Users> findAll() {
    return usersRepository.findAll();
  }

  @Override
  public Users findById(String id){
    return usersRepository.findById(id).get();
  }

  @Override
  public Users save(Users user){

   return usersRepository.save(user);
  }

}
