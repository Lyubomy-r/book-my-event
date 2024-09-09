package com.BookMyEvent.dao;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

  @Query(value = "{ 'id': ?0 }", fields = "{ 'id': 1, 'name': 1, 'email': 1, 'mailConfirmation': 1,'role': 1, 'creationDate': 1, 'location': 1 }")
  Optional<UserResponseDto> findUserInfoById(String id);

  @Query(value = "{}", fields = "{  'id': 1, 'name': 1, 'email': 1,'mailConfirmation': 1,'role': 1, 'creationDate': 1, 'location': 1 }")
  List<UserResponseDto> findAllUserProfiles();

  Optional<User> findUserByEmail(String userEmail);

  Optional<UserResponseDto> findUserInfoByEmail(String userEmail);
  boolean existsByEmail(String id);

}
