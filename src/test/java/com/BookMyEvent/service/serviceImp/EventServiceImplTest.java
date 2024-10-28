package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

  @Mock
  private EventRepository eventRepository;

  @InjectMocks
  private EventServiceImpl eventService;

  private Event event;

  @BeforeEach
  void setUp() {
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
  @DisplayName("Test EventServiceImpl method getEvents")
  void testMethodGetEvents() {

    when(eventRepository.findAll()).thenReturn(List.of(event));
    List<Event> result = eventService.getEvents();
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId(), result.get(0).getId())
    );

    verify(eventRepository, times(1)).findAll();
  }
}