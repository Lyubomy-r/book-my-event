package com.BookMyEvent.dao;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

  @Query(value = "{ 'id': ?0 }", fields = "{ 'id': 1, 'name': 1, 'email': 1, 'creationDate': 1, 'location': 1, 'status': 1}")
  Optional<UserResponseDto> findUserInfoById(ObjectId id);

  @Query(value = "{}", fields = "{ 'id': 1, 'name': 1, 'email': 1, 'creationDate': 1, 'location': 1, 'status': 1}")
  List<UserResponseDto> findAllUserProfiles();

  @Query(value = "{ 'email': ?0 }", fields = "{ 'id': 1, 'name': 1, 'email': 1, 'creationDate': 1, 'location': 1, 'status': 1 }")
  Optional<UserResponseDto> findUserInfoByEmail(String userEmail);

  boolean existsByEmail(String email);
}
