package com.BookMyEvent.config;

import com.BookMyEvent.converter.LocalTimeToStringConverter;
import com.BookMyEvent.converter.StringToLocalTimeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfig {

  @Bean
  public MongoCustomConversions mongoCustomConversions() {
    return new MongoCustomConversions(Arrays.asList(new StringToLocalTimeConverter(), new LocalTimeToStringConverter()));
  }

}