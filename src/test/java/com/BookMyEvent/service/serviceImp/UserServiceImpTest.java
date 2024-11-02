package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.control.MappingControl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class UserServiceImpTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserServiceImp userServiceImp;

  private User userOne;

  @BeforeEach
  void createUserOne() {

    userOne = User.builder()
        .id(new ObjectId("66c648b600179737a3d5c235"))
        .name("Ronald")
        .email("sewewt@code.com")
        .password("As123ertyuer")
        .location("Kyiv")
        .mailConfirmation(true)
        .status(Status.ACTIVE)
        .role(Role.VISITOR)
        .creationDate(LocalDateTime.now())
        .build();
  }

  @Test
  void findAllUserProfiles() {
  }

  @Test
  void findUserInfoById() {
  }

  @Test
  void findUserInfoByEmail() {
  }

  @Test
  void save() {
  }

  @Test
  void updateFieldsFromAdmin() {
  }

  @Test
  void delete() {
  }

  @Test
  void findAllUsers() {
  }

  @Test
  void banned() {
    when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));
    when(userRepository.save(userOne)).thenReturn(userOne);

    String user = userServiceImp.banned(userOne.getEmail());

    assertEquals("User status updated to 'BANNED'", user);
  }

  @Test
  void unban() {
    userOne.setStatus(Status.BANNED);
    when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));
    when(userRepository.save(userOne)).thenReturn(userOne);

    String user = userServiceImp.unban(userOne.getEmail());

    assertEquals("User activated successfully", user);
  }
}