package com.BookMyEvent.service;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.LoginResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.io.IOException;

public interface GoogleAuthenticationService {
  GoogleIdToken.Payload validate(String idTokenString);

  User getUserInfo(String accessToken) throws IOException;

  LoginResponse googleLogin(String idToken) throws IOException;

}
