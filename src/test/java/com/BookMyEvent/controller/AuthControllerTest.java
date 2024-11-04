package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.entity.dto.LoginResponse;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.service.AuthService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    locations = "classpath:integrationtest.properties")
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  @BeforeEach
  public void setup() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  @DisplayName("Test AuthController method Registration Positive Scenario")
  void testMethodRegistrationPositiveScenario() throws Exception {

    UserSaveDto userSaveDto = UserSaveDto.builder()
        .name("Ronald")
        .email("sewewt@code.com")
        .password("As123ertyuer")
        .build();
    String successMessage = "User registered successfully.";
    String requestBody = objectMapper.writeValueAsString(userSaveDto);

    when(authService.userRegistration(userSaveDto)).thenReturn(successMessage);

    mockMvc.perform(post("/authorize/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message", is(successMessage)));
//        .andExpect(jsonPath("$.message").value("Your comment was added successfully."));
  }

  @Test
  void mailConfirmation() {
  }

  @Test
  void login() throws Exception {
    LoginDto loginDto = LoginDto.builder()
        .email("sewewt@code.com")
        .password("As123ertyuer")
        .build();

    LoginResponse loginResponse = new LoginResponse(
        "66c648b600179737a3d5c235",
        "Ronald",
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbm9ueW1vdXNVc2VyIiwicm" +
            "9sZSI6IkFETUlOIiwiaWF0IjoxNzI4NzM2MzcwLCJleHAiOjE3Mjg3NzIzNzB9.sQs" +
            "6sV6GhkBXLkRs5JyiOW7SN0YdfFpyu7HrRp7x8PFFyq_biyHut4eTKynzMSbVwQCDRqFlL_b88RsverSmBA",
        String.format("Email (%s) is confirmed",
            loginDto.getEmail()),
        200


    );
    String requestBody = objectMapper.writeValueAsString(loginDto);

    when(authService.login(loginDto)).thenReturn(loginResponse);

    mockMvc.perform(post("/authorize/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.userId", is(loginResponse.getUserId())));
  }

  @Test
  void checkExistEmail() {
    
  }
}