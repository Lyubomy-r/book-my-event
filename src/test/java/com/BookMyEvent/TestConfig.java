package com.BookMyEvent;

import com.BookMyEvent.mapper.EventMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public EventMapperImpl eventMapper() {
    return new EventMapperImpl();
  }
}
