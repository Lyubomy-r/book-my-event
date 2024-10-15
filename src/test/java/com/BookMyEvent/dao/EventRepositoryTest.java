package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@TestPropertySource(
    locations = "classpath:integrationtest.properties")
class EventRepositoryTest {

  @Autowired
  private EventRepository eventRepository;

  private Event event;

  @BeforeEach
  void setUp() {
    eventRepository.deleteAll();

    event = new Event();
    event.setId("66c648b600179737a3d5c235");
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setAvailableTickets(100);
    event.setNumberOfTickets(100);
    event.setEventStartDate(LocalDateTime.now().minusDays(1));


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
    LocalDateTime now = LocalDateTime.now();
    List<Event> results = eventRepository.findByEventStartDate(now);
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getId()).isEqualTo(event.getId());
  }
}