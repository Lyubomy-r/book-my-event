package com.BookMyEvent.service;

import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;

import java.util.List;

public interface EventService {
    EventDTO createEvent(EventDTO eventDTO);
    EventDTO updateEvent(String eventId,EventDTO eventDTO);
    List<EventResponseDto> getEventsUA();
    void deleteEvent(String id);
    EventDTO getEventById(String id);
    void deletePastEvents();
    List<EventDTO> getAllEvents();
    EventDTO updateEventStatus(String id, EventStatus status);
    List<Event> getEventsByStatus(EventStatus status);
    EventDTO approveEvent(String id);
}
