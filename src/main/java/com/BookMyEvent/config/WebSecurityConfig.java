package com.BookMyEvent.config;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.security.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

  @Value("${front.url}")
  private String frontUrl;

  private final JwtTokenFilter JwtTokenFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsFilterRegistrationBean()))
        .authorizeHttpRequests(
            (requests) ->
                requests
                    .requestMatchers("/secured/**")
                    .authenticated()
                    .requestMatchers(GET, "/liked-events/count/event/{eventId}")
                    .permitAll()
                    .requestMatchers("/users/**", "/liked-events/**", "/liked-events/**")
                    .hasAnyRole(
                        Role.VISITOR.toString(),
                        Role.ADMIN.toString(),
                        Role.ORGANIZER.toString(),
                        Role.DEMO_ADMIN.toString())
                    .requestMatchers(POST, "/events/filtered")
                    .permitAll()
                    .requestMatchers(POST, "/events")
                    .hasAnyRole(
                        Role.VISITOR.toString(),
                        Role.ADMIN.toString(),
                        Role.ORGANIZER.toString(),
                        Role.DEMO_ADMIN.toString())
                    .requestMatchers("/organizer/**")
                    .hasRole(Role.ORGANIZER.toString())
                    .requestMatchers("/admin/**")
                    .hasRole(Role.ADMIN.toString())
                    .requestMatchers(
                        GET,
                        "/admin/users",
                        "/admin/users/{userId}",
                        "/admin/users/{userId}/events")
                    .hasRole(Role.ADMIN.toString())
                    .requestMatchers(
                        PATCH,
                        "/admin/{id}/approve",
                        "/admin/users/ban/{email}",
                        "/admin/users/unban/{email}",
                        "/admin/events/{eventsId}/status",
                        "/admin/events/{eventsId}")
                    .hasRole(Role.ADMIN.toString())
                    .requestMatchers(
                        DELETE,
                        "/admin/users/{userId}",
                        "/admin/events/{eventsId}/update/{updateRequestId}",
                        "/admin/img/img",
                        "/admin/events/{eventId}/cancel/{deleteRequestId}")
                    .hasRole(Role.ADMIN.toString())
                    .requestMatchers(PUT, "/admin/events/{eventsId}")
                    .hasRole(Role.ADMIN.toString())
                    .requestMatchers(
                        GET,
                        "/admin/events",
                        "/admin/events/status/{status}",
                        "/admin/events/count/status",
                        "/admin/events/cancel/request/{eventId}",
                        "/admin/events/update/request/{eventId}",
                        "/admin/events/{eventId}")
                    .hasAnyRole(Role.ADMIN.toString(), Role.DEMO_ADMIN.toString())
                    .requestMatchers(DELETE, "/events/{id}")
                    .hasRole(Role.ADMIN.toString())
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(JwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsFilterRegistrationBean() {

    CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowedOrigins(
        List.of(
            "http://localhost:5173",
            frontUrl,
            "https://adjacent-lethia-sergiomail-580b8292.koyeb.app"));
    cors.setAllowedMethods(
        List.of(GET.name(), POST.name(), DELETE.name(), PATCH.name(), PUT.name(), OPTIONS.name()));
    cors.setAllowedHeaders(List.of(ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, "X-ID-Token"));
    cors.setExposedHeaders(
        List.of(CONTENT_TYPE, CACHE_CONTROL, CONTENT_LANGUAGE, CONTENT_LENGTH, LAST_MODIFIED));
    cors.setAllowCredentials(true);
    cors.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return source;
  }
}
