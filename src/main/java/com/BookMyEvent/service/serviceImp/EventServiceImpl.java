package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.entity.DateDetails;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
        log.info("EventServiceImpl::createEvent - Creating new event: {}", eventDTO);
        Event event = eventMapper.toEvent(eventDTO);
        event.setCreationDate(LocalDateTime.now());
        event.setAvailableTickets(event.getNumberOfTickets());
        Event savedEvent = eventRepository.save(event);
        log.info("EventServiceImpl::createEvent - Event created successfully: {}", savedEvent);
        return eventMapper.toEventDTO(savedEvent);
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
    public List<EventResponseDto> getEventsUA() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("EventServiceImpl::getEventsUA - Fetching all events");
        List<Event> events = eventRepository.findAll();
        try {
            List<EventResponseDto> eventDTOs = events.stream()
                .map(event-> {
                    DateDetails formatDate = formatDate(event.getDate());
                    return eventMapper.toEventResponseDtoFromEvent(event, formatDate);
                })
                .toList();

            log.info("EventServiceImpl::getEvents - Found {} events", events.size());
            return eventDTOs;
        }catch (Exception e){

            log.info("{}}::{} - Exception  {} events",this.getClass().getSimpleName(), methodName, e.getMessage());
            throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
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

    @Override
    public List<EventDTO> getAllEvents() {
        log.info("EventServiceImpl::getAllEvents - Fetching all events");
        List<Event> events = eventRepository.findAll();
        List<EventDTO> eventDTOs = events.stream().map(eventMapper::toEventDTO).toList();
        log.info("EventServiceImpl::getAllEvents - Found {} events", eventDTOs.size());
        return eventDTOs;
    }

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("uk"));
        String dayText = LocalDate.parse(date.day()).format(formatter);
        return new DateDetails(dayText, date.time());
    }
}