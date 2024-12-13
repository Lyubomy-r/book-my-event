package com.BookMyEvent.service;

import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EventService {
    public EventDTO createEvent(EventDTO eventDTO, MultipartFile image);
    EventDTO updateEvent(String eventId,EventDTO eventDTO);
    List<EventResponseDto> getEventsUA();
    void deleteEvent(String id);
    EventDTO getEventById(String id);
    void deletePastEvents();
    List<EventDTO> getAllEvents();
    EventResponseDto updateEventStatus(String id, String status);
    List<EventResponseDto> getEventsByStatus(String status);
    EventResponseDto approveEvent(String id);
    EventDTO cancelEvent(String id);
    Map<EventStatus,Integer> countByStatus();
}
