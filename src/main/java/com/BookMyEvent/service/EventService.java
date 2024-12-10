package com.BookMyEvent.service;

import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;

import java.util.List;
import java.util.Optional;

public interface EventService {
    EventDTO createEvent(EventDTO eventDTO);
    EventDTO updateEvent(String eventId,EventDTO eventDTO);
    List<EventResponseDto> getEventsUA();
    void deleteEvent(String id);
    EventDTO getEventById(String id);
    void deletePastEvents();
    List<EventDTO> getAllEvents();
    EventDTO updateEventStatus(String id, String status);
    List<Event> getEventsByStatus(String status);
    EventDTO approveEvent(String id);
    EventDTO cancelEvent(String id);
    Integer countByStatus(String status);
}
