package com.BookMyEvent.service;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventFilterRequest;
import com.BookMyEvent.entity.dto.EventResponseDto;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EventService {
    public EventResponseDto createEvent(EventDTO eventDTO,
                                        MultipartFile firstImage,
                                        MultipartFile secondImage,
                                        MultipartFile thirdImage);

    EventResponseDto updateEvent(String eventId, EventDTO eventDTO, String userId);

    Page<EventResponseDto> getApprovedEvents(Pageable pageable);

    void deleteEvent(String id);

    EventResponseDto getEventById(String eventId);

    EventResponseDto getApprovedEventById(String eventId);

    void deletePastEvents();

    Page<EventResponseDto> getAllEvents(Pageable pageable);

    EventResponseDto updateEventStatus(String id, String status, String urlToEvent);

    EventResponseDto updateEventImage(String id, MultipartFile eventImage);

    Page<EventResponseDto> getEventsByStatus(String status, Pageable pageable);

    Page<EventResponseDto> getByOrganizersId(String organizerId, Pageable pageable);

    EventResponseDto approveEvent(String id);

    //    EventDTO cancelEvent(String id);
    Map<String, Integer> countByStatus();

    Page<EventResponseDto> filterEvents(EventFilterRequest filter, Pageable pageable);

    void deleteNotLinkedImg();

//    void chdb();

    void chdbConrdinatis();
}
