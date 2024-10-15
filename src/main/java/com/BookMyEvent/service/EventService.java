package com.BookMyEvent.service;

import com.BookMyEvent.entity.dto.EventDTO;

import java.util.List;

public interface EventService {
    EventDTO createEvent(EventDTO eventDTO);
    EventDTO updateEvent(String eventId,EventDTO eventDTO);
    List<EventDTO> getEvents();


    void deleteEvent(String id);

    EventDTO getEventById(String id);
    void deletePastEvents();
    List<EventDTO> getAllEvents();
}
