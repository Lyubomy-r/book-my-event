package com.BookMyEvent.security;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.UserService;
import com.BookMyEvent.service.serviceImp.GoogleAuthenticationServiceImp;
import com.BookMyEvent.service.serviceImp.JwtAuthentication;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtAuthentication jwtAuthentication;
  private final GoogleAuthenticationServiceImp googleAuthenticationServiceImp;
  private final UserService userService;
  private final String className = this.getClass().getSimpleName();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var jwt = jwtAuthentication.extractJwtFromRequest(request);
    try {
      if (jwt != null) {
        log.info("{}::doFilterInternal. extractJwtFromRequest {}", className, jwt);
        if (jwtAuthentication.hasIssuerFromToken(jwt)
            && jwtAuthentication.getIssuerFromToken(jwt).equals("https://accounts.evently-book.com")
            && jwtAuthentication.validateToken(jwt)) {
          var username = jwtAuthentication.getUsernameFromToken(jwt);
          var role = jwtAuthentication.getRoleFromToken(jwt);
          var userId = jwtAuthentication.getUserIdFromToken(jwt);
          log.info("{}::doFilterInternal.  getRoleFromToken {}", className, role);
          Optional<User> existUser = userService.findById(userId);
          if (existUser.isPresent()) {
            log.info(
                "{}::doFilterInternal. set authentication to contextHolder from findById()/ id {} ",
                className,
                existUser.get().getId().toString());
            setAuthenticationToContextHolder(existUser.get());
          }
        } else {
          log.info("{}::doFilterInternal.  try  pars jwt like GoogleIdToken.", className);
          GoogleIdToken.Payload payload = googleAuthenticationServiceImp.validate(jwt);
          String email = payload.getEmail();
          String sub = payload.getSubject();
          log.info(
                  "{}::doFilterInternal. payload {} ",
                  className,
                  payload);
          Optional<User> user = userService.findByGoogleId(sub);
          if (user.isPresent()) {
            log.info(
                "{}::doFilterInternal. set authentication to contextHolder from findByGoogleId()/ id {} ",
                className,
                user.get().getId().toString());
            setAuthenticationToContextHolder(user.get());
          } else {
            Optional<User> userFromEmail = userService.findByEmail(email);
            if (userFromEmail.isPresent()) {
              log.info(
                  "{}::doFilterInternal. set authentication to contextHolder from findByEmail()/ id {} ",
                  className,
                  userFromEmail.get().getId().toString());
              setAuthenticationToContextHolder(userFromEmail.get());
            }
          }
        }
      } else {
        log.info("{}::doFilterInternal. jwtAuthentication token is null.", className);
      }

    } catch (GeneralException e) {
      log.info(
          "{}::doFilterInternal.  error after jwtAuthentication  token is null or not correct. message ({})",
          className,
          e.getMessage());
    }
    //    try {
    //      log.info("{}::doFilterInternal.  try  pars jwt like GoogleIdToken.", className);
    //      GoogleIdToken.Payload payload = googleAuthenticationServiceImp.validate(jwt);
    ////      String accessTokenHeader = request.getHeader("X-ACCESS-TOKEN");
    //      String email = payload.getEmail();
    //      String sub = payload.getSubject();
    ////      User googleUser = googleAuthenticationServiceImp.getUserInfo(accessTokenHeader);
    ////      if (email.equals(googleUser.getEmail()) && sub.equals(googleUser.getGoogleId())) {
    //      Optional<User> user =
    //              userService.findByGoogleId(sub);
    //      if(user.isPresent()){
    //        log.info("{}::doFilterInternal. set authentication to contextHolder from
    // findByGoogleId()/ id {} ",
    //                className, user.get().getId().toString());
    //        setAuthenticationToContextHolder(user.get());
    //      }else {
    //        Optional<User> userFromEmail = userService.findByEmail(email);
    //        if(userFromEmail.isPresent()){
    //          log.info("{}::doFilterInternal. set authentication to contextHolder from
    // findByEmail()/ id {} ",
    //                  className, userFromEmail.get().getId().toString());
    //          setAuthenticationToContextHolder(userFromEmail.get());
    //        }
    //      }
    ////      }
    //    } catch (GeneralException e) {
    //      log.info("{}::doFilterInternal. extractJwtFromRequest {}", className, jwt);
    //      try {
    //        if (StringUtils.hasText(jwt)
    //                &&
    // jwtAuthentication.getIssuerFromToken(jwt).equals("https://accounts.evently-book.com")
    //                && jwtAuthentication.validateToken(jwt)) {
    //          var username = jwtAuthentication.getUsernameFromToken(jwt);
    //          var role = jwtAuthentication.getRoleFromToken(jwt);
    //          var userId = jwtAuthentication.getUserIdFromToken(jwt);
    //          log.info("{}::doFilterInternal.  getRoleFromToken {}", className, role);
    //          Optional<User> existUser = userService.findById( userId);
    //          if(existUser.isPresent()){
    //            log.info("{}::doFilterInternal. set authentication to contextHolder from
    // findById()/ id {} ",
    //                    className, existUser.get().getId().toString());
    //            setAuthenticationToContextHolder(existUser.get());
    //          }
    ////          List<GrantedAuthority> authorities = new ArrayList<>();
    ////          authorities.add(new SimpleGrantedAuthority(role));
    ////
    ////          Map<String, Object> principal = new HashMap<>();
    ////          principal.put("username", username);
    ////          principal.put("id", userId);
    ////
    ////          UsernamePasswordAuthenticationToken authentication =
    ////              new UsernamePasswordAuthenticationToken(principal, null, authorities);
    ////          SecurityContextHolder.getContext().setAuthentication(authentication);
    //        }
    //      } catch (Exception ex) {
    //        log.error("{}::doFilterInternal.  error after jwtAuthentication.", className);
    //      }
    //    }

    filterChain.doFilter(request, response);
  }

  private static void setAuthenticationToContextHolder(User user) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    String role = "ROLE_" + user.getRole().toString();
    authorities.add(new SimpleGrantedAuthority(role));
    SecurityUser principal = new SecurityUser(user);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, authorities);
    log.info("Principal id={} username={}", principal.getId(), principal.getUsername());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
