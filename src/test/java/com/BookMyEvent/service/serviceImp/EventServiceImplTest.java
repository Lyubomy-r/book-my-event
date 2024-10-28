package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.mapper.EventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class EventServiceImplTest {

  @Mock
  private EventRepository eventRepository;

  @InjectMocks
  private EventServiceImpl eventService;

  @Mock
  private EventMapper eventMapper;

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
    EventResponseDto eventResponseDto =  new EventResponseDto();
    eventResponseDto.setId("66c648b600179737a3d5c235");
    eventResponseDto.setTitle("Test Event");
    eventResponseDto.setDescription("Test Description");
    eventResponseDto.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    eventResponseDto.setEventCategory(EventCategory.TOP_EVENTS.toString());
    eventResponseDto.setAvailableTickets(100);
    eventResponseDto.setNumberOfTickets(100);

    eventResponseDto.setDate(new DateDetails(LocalDate.now().toString(), LocalTime.now().toString()));
    when(eventRepository.findAll()).thenReturn(List.of(event));
    when(eventMapper.toEventResponseDtoFromEvent(event)).thenReturn(eventResponseDto);
    List<EventResponseDto> result = eventService.getEventsUA();
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId(), result.get(0).getId())
    );

    verify(eventRepository, times(1)).findAll();
  }
}