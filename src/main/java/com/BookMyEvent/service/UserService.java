package com.BookMyEvent.service;

import com.BookMyEvent.entity.Users;

import java.util.List;

public interface UserService {

   List<Users> findAll();

   Users findById(String id);

   Users save(Users user);
}
