package com.BookMyEvent.service;

import com.BookMyEvent.entity.EventDeleteRequest;
import com.BookMyEvent.entity.EventUpdateRequest;
import com.BookMyEvent.entity.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface EventService {
    public EventResponseDto createEvent(EventDTO eventDTO,
                                        MultipartFile firstImage,
                                        MultipartFile secondImage,
                                        MultipartFile thirdImage);

    String createUpdateEventRequest(String id, EventUpdateDTO eventDTO, String userId,
                                    MultipartFile secondImage,
                                    MultipartFile thirdImage);

    EventResponseDto updateEvent(String eventId, String eventUpdateRequestId);

    String cancelEventUpdateRequest(String eventId, String eventUpdateRequestId);

    Page<EventResponseDto> getApprovedEvents(Pageable pageable);

    String createDeleteEventRequest(String eventId, EventDeleteRequest eventDeleteRequest, String userId);

    void deleteEvent(String eventId, String eventDeleteRequestId);

    EventResponseDto getEventById(String eventId);

    EventUpdateRequestDTO getEventUpdateRequestById(String eventId);

    EventDeleteRequest getEventCancelRequestById(String eventId);

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

    void userDeleteEvent(String eventId, String userId);

//    void chdb();

    void chdbConrdinatis();
}
