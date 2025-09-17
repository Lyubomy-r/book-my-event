package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.LoginResponse;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.GoogleAuthenticationService;
import com.BookMyEvent.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthenticationServiceImp implements GoogleAuthenticationService {

    private final GoogleIdTokenVerifier googleVerifier;
    private final UserService userService;
    private final String className = this.getClass().getSimpleName();
    private final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?access_token=";
    private final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    @Override
    public GoogleIdToken.Payload validate(String idTokenString) {
        try{
            log.info("{}::validate.  start  message.", className);
            GoogleIdToken idToken = googleVerifier.verify(idTokenString);
            log.info("{}::validate.  finish  message.", className);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                log.error("{}::validate.  Return error message.", className);
                throw new GeneralException("Invalid Google ID token", HttpStatus.FORBIDDEN);
            }
        }catch (Exception e){
            log.error("{}::validate.  Return error message ({})after google verify.", className, e.getMessage());
            throw new GeneralException("Invalid Google ID token", HttpStatus.FORBIDDEN);
        }
    }

    public User getUserInfo(String accessToken) throws IOException {

        NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
        try {
            com.google.api.client.http.HttpResponse response =
                    requestFactory
                            .buildGetRequest(new com.google.api.client.http.GenericUrl(USER_INFO_URL))
                            .execute();

            String jsonIdentity = response.parseAsString();
            JsonObject jsonObject = JsonParser.parseString(jsonIdentity).getAsJsonObject();
            log.info("jsonObject: " + jsonObject);

            String name = jsonObject.get("name").getAsString();
            String email = jsonObject.get("email").getAsString();
            String avatarUrl = jsonObject.get("picture").getAsString();
            String socialIdentifier = jsonObject.get("sub").getAsString();
//            String password = passwordEncoder.encode(name);
            String location = "";

            log.info("fullName: " + name);
            log.info("email: " + email);
            log.info("picture: " + avatarUrl);
            log.info("sub: " + socialIdentifier);
            User newUser =User.builder()
                    .googleId(socialIdentifier)
                    .email(email)
                    .name(name)
//                        .surname(surname)
                    .build();

            log.info("userInfo: " + newUser);
            log.info("GoogleOAuthService::getUserInfo." + " Return User Info from google user.");

            return newUser;
        } catch (Exception e) {
            log.info(
                    "GoogleOAuthService::getUserInfo."
                            + " Throw AuthorizedException with message (Problem with accessToken : Message({})",
                    e.getMessage());
            throw new GeneralException(
                    String.format("Problem with accessToken. Message(/%s) ", e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    public LoginResponse googleLogin(String idToken, String accessToken) throws IOException {
        log.info("{}::googleLogin.  try  pars jwt like GoogleIdToken.", className);
        GoogleIdToken.Payload payload = validate(idToken);
        String email = payload.getEmail();
        String sub = payload.getSubject();
        User googleUser = getUserInfo(accessToken);
        if (email.equals(googleUser.getEmail()) && sub.equals(googleUser.getGoogleId())) {
            User user =
                    userService.findByGoogleId(sub).orElseGet(() -> userService.saveGoogleUser(googleUser));
            log.info("{}::doFilterInternal.  get or save google user", className);
            log.info(
                    "{}::doFilterInternal.  get or save google user {}", className, user.getId().toString());
            LoginResponse tokenPair = new LoginResponse(user.getId().toHexString(),
                    user.getName(), idToken,
                    String.format("Email (%s) is confirmed",
                            user.getEmail()),
                    HttpStatus.OK.value());
            log.info("AuthServiceImp::login. Verified  accessToken and return token to user ({}).", user.getEmail());
            return tokenPair;
        }
        LoginResponse tokenPair = new LoginResponse(null,
                null, null,
                String.format("Email (%s) is not confirmed",
                        email),
                HttpStatus.CONTINUE.value());
        log.info("AuthServiceImp::login. Verified  accessToken and return token to user ({}).", email);
        return tokenPair;
    }
}
