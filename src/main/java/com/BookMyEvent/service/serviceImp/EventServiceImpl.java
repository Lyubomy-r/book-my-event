package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.EventMapper;
import com.BookMyEvent.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        log.info("EventServiceImpl::createEvent - Creating new event with pending status: {}", eventDTO);
        Event event = eventMapper.toEvent(eventDTO);
        event.setCreationDate(LocalDateTime.now());

        event.setAvailableTickets(event.getNumberOfTickets());

        Event savedEvent = eventRepository.save(event);
        log.info("EventServiceImpl::createEvent - Event created successfully: {}", savedEvent);
        return eventMapper.toEventDTO(savedEvent);
    }

    @Override
    @Transactional
    public EventResponseDto approveEvent(String id) {
        log.info("EventServiceImpl::approveEvent - Approving event with ID: {}", id);

        Event existingEvent = eventRepository.findById(id)
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));

        existingEvent.setEventStatus(EventStatus.APPROVED);

        Event approvedEvent = eventRepository.save(existingEvent);

        log.info("EventServiceImpl::approveEvent - Event approved successfully: {}", approvedEvent);


        return eventMapper.toEventResponseDtoFromEvent(approvedEvent, createDateDetails(approvedEvent));
    }


    private DateDetails createDateDetails(Event event) {

        return new DateDetails(event.getDate().day(), event.getDate().time(), event.getDate().endTime());
    }

    @Override
    @Transactional
    public EventDTO updateEvent(String id, EventDTO eventDTO) {
        log.info("EventServiceImpl::updateEvent - Updating event ID: {} with data: {}", id, eventDTO);
        Event existingEvent = eventRepository.findById(id)
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
        eventMapper.updateEventFromDTO(eventDTO, existingEvent);
        Event updatedEvent = eventRepository.save(existingEvent);
        log.info("EventServiceImpl::updateEvent - Event updated successfully: {}", updatedEvent);
        return eventMapper.toEventDTO(updatedEvent);
    }

    @Override
    @Transactional
    public EventDTO cancelEvent(String id) {
        log.info("EventServiceImpl::cancelEvent - Cancelling event with ID: {}", id);

        Event existingEvent = eventRepository.findById(id)
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));

        existingEvent.setEventStatus(EventStatus.CANCELLED);

        Event cancelledEvent = eventRepository.save(existingEvent);

        log.info("EventServiceImpl::cancelEvent - Event cancelled successfully: {}", cancelledEvent);
        return eventMapper.toEventDTO(cancelledEvent);
    }

    @Override
    public List<EventResponseDto> getEventsUA() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("EventServiceImpl::getEventsUA - Fetching all APPROVED events");
        List<Event> events = eventRepository.findEventByEventStatus(EventStatus.APPROVED);
        try {
            List<EventResponseDto> eventDTOs = events.stream()
                .map(event-> {
                    DateDetails formatDate = formatDate(event.getDate());
                    return eventMapper.toEventResponseDtoFromEvent(event, formatDate);
                })
                .toList();

            log.info("EventServiceImpl::getEvents - Found {} events", events.size());
            return eventDTOs;
        } catch (Exception e) {

            log.info("{}}::{} - Exception  {} events", this.getClass().getSimpleName(), methodName, e.getMessage());
            throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<EventResponseDto> getAllEvents() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("{}::getEventsUA - Fetching all events", methodName);
        List<Event> events = eventRepository.findAll();
        try {
            List<EventResponseDto> eventDTOs = events.stream()
                .sorted((e1, e2) -> {
                    if (e1.getEventCategory() == EventCategory.TOP_EVENTS
                        && e2.getEventCategory() != EventCategory.TOP_EVENTS) {
                        return -1;
                    } else if (e1.getEventCategory() != EventCategory.TOP_EVENTS
                        && e2.getEventCategory() == EventCategory.TOP_EVENTS) {
                        return 1;
                    }
                    return e1.getCreationDate().compareTo(e2.getCreationDate());
                })
                .map(event -> {
                    DateDetails formatDate = formatDate(event.getDate());
                    return eventMapper.toEventResponseDtoFromEvent(event, formatDate);
                })

                .toList();

            log.info("EventServiceImpl::getEvents - Found {} events", events.size());
            return eventDTOs;
        } catch (Exception e) {

            log.info("{}}::{} - Exception  {} events", this.getClass().getSimpleName(), methodName, e.getMessage());
            throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Map<String, Integer> countByStatus() {
        log.info("EventServiceImpl::countByStatus - Start counting events for all statuses");

        Map<String, Integer> statusCountMap = Arrays.stream(EventStatus.values())
            .collect(Collectors.toMap(
                Enum::toString,
                status -> eventRepository.findEventByEventStatus(status).size()
            ));

        log.info("EventServiceImpl::countByStatus - Events count by status: {}", statusCountMap);
        return statusCountMap;
    }


    private EventStatus parseEventStatus(String status) {
        try {
            return EventStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", status, e);
            throw new GeneralException("Invalid status value or such status doesn't exist: " + status, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public void deleteEvent(String id) {
        log.info("EventServiceImpl::deleteEvent - Deleting event ID: {}", id);
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
        eventRepository.delete(event);
        log.info("EventServiceImpl::deleteEvent - Event marked as deleted: {}", id);
    }

    @Override
    public EventDTO getEventById(String id) {
        log.info("EventServiceImpl::getEventById - Fetching event ID: {}", id);
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
        EventDTO eventDTO = eventMapper.toEventDTO(event);
        log.info("EventServiceImpl::getEventById - Found event: {}", eventDTO);
        return eventDTO;
    }

//    @Override
//    public List<EventDTO> getAllEvents() {
//        log.info("EventServiceImpl::getAllEvents - Fetching all events");
//        List<Event> events = eventRepository.findAll();
//        List<EventDTO> eventDTOs = events.stream().map(eventMapper::toEventDTO).toList();
//        log.info("EventServiceImpl::getAllEvents - Found {} events", eventDTOs.size());
//        return eventDTOs;
//    }

    @Override
    @Transactional
    public void deletePastEvents() {
        log.info("EventServiceImpl::deletePastEvents - Deleting past events...");
        LocalDate now = LocalDate.now().minusDays(1);
        List<Event> pastEvents = eventRepository.findByDateDay(now.toString());
//        LocalDateTime now = LocalDateTime.now();
//        List<Event> pastEvents = eventRepository.findByEndDateBefore(now);
        if (pastEvents.isEmpty()) {
            log.info("EventServiceImpl::deletePastEvents - No past events found for deletion.");
        } else {
            eventRepository.deleteAll(pastEvents);
            log.info("EventServiceImpl::deletePastEvents - Deleted {} past events.", pastEvents.size());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledDeletePastEvents() {
        log.info("EventServiceImpl::scheduledDeletePastEvents - Running scheduled task to delete past events");
        deletePastEvents();
    }


    public DateDetails formatDate(DateDetails date) {
        if (date == null) {
            return null;
        }

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("uk"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

        String formattedDay = LocalDate.parse(date.day()).format(dayFormatter);
        String formattedTime = date.time() != null ? LocalTime.parse(date.time()).format(timeFormatter) : null;
        String formattedEndTime = date.endTime() != null ? LocalTime.parse(date.endTime()).format(timeFormatter) : null;
        log.info("EventServiceImpl::scheduledDeletePastEvents - formattedDay {}  - formattedTime {}  -formattedEndTime {} ",
            formattedDay,
            formattedTime,
            formattedEndTime
        );
        return new DateDetails(formattedDay, formattedTime, formattedEndTime);
    }


    @Override
    @Transactional
    public EventResponseDto updateEventStatus(String id, String status) {
        log.info("EventServiceImpl::updateEventStatus - Updating event ID: {} with new status: {}", id, status);

        Event existingEvent = eventRepository.findById(id)
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));

        EventStatus newStatus = parseEventStatus(status);

        if (existingEvent.getEventStatus() == EventStatus.CANCELLED) {
            log.error("Cannot update status for cancelled event: {}", id);
            throw new GeneralException("Cannot update status for a cancelled event", HttpStatus.BAD_REQUEST);
        }

        existingEvent.setEventStatus(newStatus);
        Event updatedEvent = eventRepository.save(existingEvent);

        log.info("EventServiceImpl::updateEventStatus - Event status updated successfully: {}", updatedEvent);

        DateDetails formattedDateDetails = formatDate(updatedEvent.getDate());
        return eventMapper.toEventResponseDtoFromEvent(updatedEvent, formattedDateDetails);
    }

    @Override
    public List<EventResponseDto> getEventsByStatus(String status) {
        log.info("Fetching events with status: {}", status);

        EventStatus eventStatus;
        try {
            eventStatus = EventStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException("Invalid status value or such status doesn't exist: " + status, HttpStatus.BAD_REQUEST);
        }

        List<Event> events = eventRepository.findEventByEventStatus(eventStatus);
        return events.stream()
            .map(event -> eventMapper.toEventResponseDtoFromEvent(event, formatDate(event.getDate())))
            .collect(Collectors.toList());
    }


}