package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
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
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;

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
    event.setDate(new DateDetails(LocalDate.of(2025, 10, 21).toString(), LocalTime.now().toString(),LocalTime.now().plus(5, ChronoUnit.HOURS).toString()));
  }

  @Test
  @DisplayName("Test EventServiceImpl method getEvents")
  void testMethodGetEvents() {
    EventResponseDto eventResponseDto = new EventResponseDto();
    eventResponseDto.setId("66c648b600179737a3d5c235");
    eventResponseDto.setTitle("Test Event");
    eventResponseDto.setDescription("Test Description");
    eventResponseDto.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    eventResponseDto.setEventCategory(EventCategory.TOP_EVENTS.toString());
    eventResponseDto.setAvailableTickets(100);
    eventResponseDto.setNumberOfTickets(100);
    DateDetails dateDetails = new DateDetails("21 жовтня", event.getDate().time(),event.getDate().endTime());
    eventResponseDto.setDate(dateDetails);

    when(eventRepository.findAll()).thenReturn(List.of(event));
    when(eventMapper.toEventResponseDtoFromEvent(event, dateDetails)).thenReturn(eventResponseDto);
    List<EventResponseDto> result = eventService.getEventsUA();
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId(), result.get(0).getId())
    );

    verify(eventRepository, times(1)).findAll();
  }

  @Test
  void testUpdateEventStatus() {
    String eventId = "66c648b600179737a3d5c235";

    EventDTO updatedEventDTO = new EventDTO();
    updatedEventDTO.setId(eventId);
    updatedEventDTO.setEventStatus(EventStatus.APPROVED);

    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(eventRepository.save(event)).thenReturn(event);
    when(eventMapper.toEventDTO(event)).thenReturn(updatedEventDTO);

    EventResponseDto result = eventService.updateEventStatus(eventId, EventStatus.APPROVED.toString());

    assertEquals( EventStatus.APPROVED.toString(), result.getEventStatus());
    verify(eventRepository, times(1)).save(event);
  }
}