package com.BookMyEvent.controller;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@TestPropertySource(locations = "classpath:integrationtest.properties")
class EventControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private EventService eventService;

  private EventResponseDto event = new EventResponseDto();

  @BeforeEach
  void setUp() {
    LocalTime localTime = LocalTime.now();
    event.setId("66c648b600179737a3d5c235");
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    event.setEventCategory(EventCategory.TOP_EVENTS.toString());
    event.setAvailableTickets(100);
    event.setNumberOfTickets(0);
    event.setDate(
        new DateDetails(
            LocalDate.of(2025, 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
  }

  @Nested
  @DisplayName("Test EventController method getApprovedEvents.")
  class GetApprovedEvents {

    @Test
    @DisplayName(
        "Test EventController method getApprovedEvents without param city name. Positive Scenario return events.")
    void testMethodGetApprovedEventsPositiveScenarioWithoutParamCityName() throws Exception {
      Pageable pageable = PageRequest.of(0, 6);
      EventResponseDto event = new EventResponseDto();
      List<EventResponseDto> results = List.of(event);
      PageImpl<EventResponseDto> page = new PageImpl<>(results, pageable, results.size());

      when(eventService.getApprovedEvents(any(Pageable.class), eq(null))).thenReturn(page);

      mockMvc
          .perform(get("/events"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content.[*].id", containsInAnyOrder(event.getId())))
          .andExpect(jsonPath("$.content.[0].type").value(event.getEventType()));
    }

    @Test
    @DisplayName(
        "Test EventController method getApprovedEvents with param city name. Positive Scenario return events.")
    void testMethodGetApprovedEventsPositiveScenarioWithParamCityName() throws Exception {
      String cityName = "Київ";
      Pageable pageable = PageRequest.of(0, 6);
      List<EventResponseDto> results = List.of(event);
      PageImpl<EventResponseDto> page = new PageImpl<>(results, pageable, results.size());

      when(eventService.getApprovedEvents(any(Pageable.class), eq(cityName))).thenReturn(page);

      mockMvc
          .perform(get("/events").param("cityName", cityName))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content.[*].id", containsInAnyOrder(event.getId())))
          .andExpect(jsonPath("$.content.[0].type").value(event.getEventType()));
    }
  }

  @Nested
  @DisplayName("Test EventController method getNewEvents.")
  class GetNewEvents {

    @Test
    @DisplayName(
        "Test EventController method getNewEvents without param city name. Positive Scenario return events.")
    void testMethodGetNewEventsPositiveScenarioWithoutParamCityName() throws Exception {
      EventResponseDto event = new EventResponseDto();
      List<EventResponseDto> results = List.of(event);

      when(eventService.getNewEvents(2, null)).thenReturn(results);

      mockMvc
          .perform(get("/events/new").param("size", String.valueOf(2)))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$.[*].id", containsInAnyOrder(event.getId())))
          .andExpect(jsonPath("$.[0].type").value(event.getEventType()));
    }

    @Test
    @DisplayName(
        "Test EventController method getNewEvents with param city name. Positive Scenario return events.")
    void testMethodGetNewEventsPositiveScenarioWithParamCityName() throws Exception {
      String cityName = "Київ";
      List<EventResponseDto> results = List.of(event);

      when(eventService.getNewEvents(2, cityName)).thenReturn(results);

      mockMvc
          .perform(get("/events/new").param("size", "2").param("cityName", cityName))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$.[*].id", containsInAnyOrder(event.getId())))
          .andExpect(jsonPath("$.[0].type").value(event.getEventType()));
    }

    @Test
    @DisplayName(
            "Test EventController method getNewEvents. Negative Scenario throw exception when cityName was entered incorrectly.")
    void testMethodGetNewEventsNegativeScenarioThrowException() throws Exception {
      String cityName = "cityName";
      String errorMessage = "The city name was entered incorrectly.";
      GeneralException exception = new GeneralException(errorMessage, HttpStatus.BAD_REQUEST);
      when(eventService.getNewEvents(2, cityName)).thenThrow(exception);

      mockMvc
              .perform(get("/events/new").param("size", "2").param("cityName", cityName))
              .andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
              .andExpect(jsonPath("$.message").value(errorMessage));
    }
  }

  //  @Test
  //  void getAllEventsUA() throws Exception {
  //    LocalTime  localTime =  LocalTime.now();
  //    Pageable pageable = PageRequest.of(0,6);
  //    EventResponseDto event = new EventResponseDto();
  //    event.setId("66c648b600179737a3d5c235");
  //    event.setTitle("Test Event");
  //    event.setDescription("Test Description");
  //    event.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
  //    event.setEventCategory(EventCategory.TOP_EVENTS.toString());
  //    event.setAvailableTickets(100);
  //    event.setNumberOfTickets(0);
  //    event.setDate(new DateDetails(LocalDate.of(2025, 10, 21).toString(),
  //        localTime.toString(),
  //        localTime.plusHours(2L).toString()));
  //    List<EventResponseDto> results = List.of(event);
  //    PageImpl<EventResponseDto> page= new PageImpl<>(results,pageable,results.size());
  //    when(eventService.getApprovedEvents(pageable)).thenReturn(page);
  //
  //    String responseBody = objectMapper.writeValueAsString(results);
  //    log.info("results " +results);
  //    log.info("results " +responseBody);
  //
  //    mockMvc.perform(get("/events"))
  //        .andExpect(status().isOk())
  //        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
  //        .andExpect(jsonPath("$", hasSize(1)))
  //        .andExpect(jsonPath("$.content.[*].id", containsInAnyOrder(event.getId())))
  //        .andExpect(jsonPath("$.content.[0].type").value(event.getEventType()));
  //  }
  //
  //  @Test
  //  void updateEvent() throws Exception {
  //    String eventId = "66c648b600179737a3d5c235";
  //    EventDTO eventDTO = new EventDTO();
  //    eventDTO.setTitle("Updated Event");
  //    eventDTO.setDescription("Updated Description");
  //
  //    eventDTO.setNumberOfTickets(200);
  //    eventDTO.setAvailableTickets(150);
  //    eventDTO.setLocation(new Location("City", "Street", "Venue", "23,3366", "435,2787"));
  //
  //    EventResponseDto updatedEventDTO = new EventResponseDto();
  //    updatedEventDTO.setId(eventId);
  //    updatedEventDTO.setTitle("Updated Event");
  //
  //    when(eventService.updateEvent(eq(eventId), eq(eventDTO))).thenReturn(updatedEventDTO);
  //
  //    mockMvc.perform(put("/events/{id}", eventId)
  //                    .contentType(MediaType.APPLICATION_JSON)
  //                    .content(objectMapper.writeValueAsString(eventDTO)))
  //            .andExpect(status().isOk())
  //            .andExpect(jsonPath("$.id").value(eventId))
  //            .andExpect(jsonPath("$.title").value("Updated Event"));
  //  }
  //
  //  @Test
  //  void deleteEvent() throws Exception {
  //    String eventId = "66c648b600179737a3d5c235";
  //    doNothing().when(eventService).deleteEvent(eventId);
  //
  //    mockMvc.perform(delete("/events/{id}", eventId))
  //            .andExpect(status().isNoContent());
  //  }
}
