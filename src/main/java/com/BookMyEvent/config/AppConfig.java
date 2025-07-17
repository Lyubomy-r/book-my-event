package com.BookMyEvent.config;

import com.BookMyEvent.converter.LocalTimeToStringConverter;
import com.BookMyEvent.converter.StringToLocalTimeConverter;
import com.BookMyEvent.entity.CityList;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Arrays;

@Configuration
@EnableSpringDataWebSupport(
    pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AppConfig {
  @Value("${cloudinary.cloud-name}")
  private String cloudinaryCloudName;

  @Value("${cloudinary.api-key}")
  private String cloudinaryApiKey;

  @Value("${cloudinary.api-secret}")
  private String cloudinaryApiSecret;

  @Bean
  public MongoCustomConversions mongoCustomConversions() {
    return new MongoCustomConversions(
        Arrays.asList(new StringToLocalTimeConverter(), new LocalTimeToStringConverter()));
  }

  @Bean
  protected PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10);
    scheduler.setThreadNamePrefix("TaskScheduler-");
    scheduler.initialize();
    return scheduler;
  }

  @Bean
  public Cloudinary cloudinary() {
    return new Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", cloudinaryCloudName,
            "api_key", cloudinaryApiKey,
            "api_secret", cloudinaryApiSecret,
            "secure", true));
  }

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
