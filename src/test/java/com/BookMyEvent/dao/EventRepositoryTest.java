package com.BookMyEvent.dao;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.mapper.EventMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@TestPropertySource(
    locations = "classpath:integrationtest.properties")
@Import(TestConfig.class)
class EventRepositoryTest {

  @Autowired
  private EventRepository eventRepository;

  private Event event;

  @Autowired
  private EventMapper eventMapper;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    eventRepository.deleteAll();
    event = new Event();
    event.setId("66c648b600179737a3d5c235");
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS);
    event.setEventCategory(EventCategory.TOP_EVENTS);
    event.setAvailableTickets(100);
    event.setNumberOfTickets(100);
    event.setDate(new DateDetails(LocalDate.now().toString(), LocalTime.now().toString()));



  }

  @Test
  public void testFindEventNumberOfTickets() {
    eventRepository.save(event);
    Optional<Event> result = eventRepository.findEventNumberOfTickets(event.getId());
    assertTrue(result.isPresent());
    assertEquals(100, result.get().getNumberOfTickets());
  }

  @Test
  public void testFindByEventStartDate() {

    eventRepository.save(event);
    LocalDate now = LocalDate.now();
    List<Event> results = eventRepository.findByDateDay(now.toString());
    System.out.println("results " +results);
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getId()).isEqualTo(event.getId());
  }
  @Test
  public void testFindAll() throws JsonProcessingException {

    eventRepository.save(event);

    List<Event> results = eventRepository.findAll();

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getId()).isEqualTo(event.getId());
  }

}