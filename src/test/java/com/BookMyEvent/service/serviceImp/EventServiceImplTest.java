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

import java.time.format.DateTimeFormatter;

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;

import java.util.List;
import java.util.Locale;
import java.util.Map;
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

  private DateDetails dateDetails;

  DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("uk"));
  DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

  @BeforeEach
  void setUp() {
    LocalTime  localTime =  LocalTime.now();
    event = new Event();
    event.setId("66c648b600179737a3d5c235");
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS);
    event.setEventCategory(EventCategory.TOP_EVENTS);
    event.setEventStatus(EventStatus.PENDING);
    event.setAvailableTickets(100);
    event.setNumberOfTickets(100);

    event.setDate(new DateDetails(LocalDate.of(2025, 10, 21).toString(),
        localTime.toString(),
        localTime.plusHours(2L).toString()));

    dateDetails = new DateDetails(LocalDate.parse(
        event.getDate().day()).format(dayFormatter),
        LocalTime.parse(event.getDate().time()).format(timeFormatter),
        LocalTime.parse(event.getDate().endTime()).format(timeFormatter));

//    event.setDate(new DateDetails(LocalDate.of(2025, 10, 21).toString(), LocalTime.now().toString(),LocalTime.now().plus(5, ChronoUnit.HOURS).toString()));

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

//    DateDetails dateDetails = new DateDetails(LocalDate.parse(
//        event.getDate().day()).format(dayFormatter),
//        LocalTime.parse(event.getDate().time()).format(timeFormatter),
//        LocalTime.parse(event.getDate().endTime()).format(timeFormatter));

//    DateDetails dateDetails = new DateDetails("21 жовтня", event.getDate().time(),event.getDate().endTime());

    eventResponseDto.setDate(dateDetails);

    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED)).thenReturn(List.of(event));
    when(eventMapper.toEventResponseDtoFromEvent(event, dateDetails)).thenReturn(eventResponseDto);
    List<EventResponseDto> result = eventService.getEventsUA();
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId(), result.get(0).getId())
    );

    verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.APPROVED);
  }

  @Test
  void testUpdateEventStatus() {
    String eventId = "66c648b600179737a3d5c235";

    EventDTO updatedEventDTO = new EventDTO();
    updatedEventDTO.setId(eventId);
    updatedEventDTO.setEventStatus(EventStatus.APPROVED);

    EventResponseDto eventResponseDto = new EventResponseDto();
    eventResponseDto.setId("66c648b600179737a3d5c235");
    eventResponseDto.setTitle("Test Event");
    eventResponseDto.setDescription("Test Description");
    eventResponseDto.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    eventResponseDto.setEventCategory(EventCategory.TOP_EVENTS.toString());
    eventResponseDto.setEventStatus(EventStatus.APPROVED);
    eventResponseDto.setAvailableTickets(100);
    eventResponseDto.setNumberOfTickets(100);

    eventResponseDto.setDate(dateDetails);

    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(eventRepository.save(event)).thenReturn(event);
    when(eventMapper.toEventResponseDtoFromEvent(event, dateDetails)).thenReturn(eventResponseDto);

    EventResponseDto result = eventService.updateEventStatus(eventId, EventStatus.APPROVED.toString());

    assertEquals( EventStatus.APPROVED, result.getEventStatus());
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  @DisplayName("Test EventServiceImpl method GetEventsByStatus find PENDING events")
  void testMethodGetEventsByStatusPENDING() {

    EventResponseDto eventResponseDto = new EventResponseDto();
    eventResponseDto.setId("66c648b600179737a3d5c235");
    eventResponseDto.setTitle("Test Event");
    eventResponseDto.setDescription("Test Description");
    eventResponseDto.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    eventResponseDto.setEventCategory(EventCategory.TOP_EVENTS.toString());
    eventResponseDto.setEventStatus(event.getEventStatus());
    eventResponseDto.setAvailableTickets(100);
    eventResponseDto.setNumberOfTickets(100);

    eventResponseDto.setDate(event.getDate());

    when(eventRepository.findEventByEventStatus(EventStatus.PENDING)).thenReturn(List.of(event));
    when(eventMapper.toEventResponseDtoFromEvent(event, dateDetails)).thenReturn(eventResponseDto);
    List<EventResponseDto> result = eventService.getEventsByStatus(EventStatus.PENDING.toString());
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId(), result.get(0).getId()),
        () -> assertEquals(event.getEventStatus(), result.get(0).getEventStatus())
    );

    verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.PENDING);
  }

  @Test
  @DisplayName("Test EventServiceImpl method GetEventsByStatus find APPROVED events")
  void testMethodGetEventsByStatusAPPROVED() {
    event.setEventStatus(EventStatus.APPROVED);
    EventResponseDto eventResponseDto = new EventResponseDto();
    eventResponseDto.setId("66c648b600179737a3d5c235");
    eventResponseDto.setTitle("Test Event");
    eventResponseDto.setDescription("Test Description");
    eventResponseDto.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    eventResponseDto.setEventCategory(EventCategory.TOP_EVENTS.toString());
    eventResponseDto.setEventStatus(event.getEventStatus());
    eventResponseDto.setAvailableTickets(100);
    eventResponseDto.setNumberOfTickets(100);

    eventResponseDto.setDate(event.getDate());

    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED)).thenReturn(List.of(event));
    when(eventMapper.toEventResponseDtoFromEvent(event, dateDetails)).thenReturn(eventResponseDto);
    List<EventResponseDto> result = eventService.getEventsByStatus(EventStatus.APPROVED.toString());
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId(), result.get(0).getId()),
        () -> assertEquals(event.getEventStatus(), result.get(0).getEventStatus())
    );

    verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.APPROVED);
  }

  @Test
  @DisplayName("Test EventServiceImpl method countByStatus.")
  void countByStatus(){

    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED)).thenReturn(List.of(event));
    when(eventRepository.findEventByEventStatus(EventStatus.CANCELLED)).thenReturn(List.of());
    when(eventRepository.findEventByEventStatus(EventStatus.PENDING)).thenReturn(List.of(event));

    Map<String, Integer> countByStatus = eventService.countByStatus();
    assertAll(
        () -> assertFalse(countByStatus.isEmpty()),
        () -> assertEquals(1, countByStatus.get(EventStatus.APPROVED.toString())),
        () -> assertEquals(1, countByStatus.get(EventStatus.PENDING.toString())),
        () -> assertEquals(0, countByStatus.get(EventStatus.CANCELLED.toString()))
    );
  }

  @Test
  @DisplayName("Test EventServiceImpl method approveEvent.")
  void testMethodApproveEvent(){

    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED)).thenReturn(List.of(event));
    when(eventRepository.findEventByEventStatus(EventStatus.CANCELLED)).thenReturn(List.of());
    when(eventRepository.findEventByEventStatus(EventStatus.PENDING)).thenReturn(List.of(event));

    Map<String, Integer> countByStatus = eventService.countByStatus();
    assertAll(
        () -> assertFalse(countByStatus.isEmpty()),
        () -> assertEquals(1, countByStatus.get(EventStatus.APPROVED.toString())),
        () -> assertEquals(1, countByStatus.get(EventStatus.PENDING.toString())),
        () -> assertEquals(0, countByStatus.get(EventStatus.CANCELLED.toString()))
    );
  }


}