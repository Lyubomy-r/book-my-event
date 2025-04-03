package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.ImageRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.EventFilterRequest;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.EventMapper;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.CloudinaryService;
import com.BookMyEvent.service.EventService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.UserLikedEventService;
import com.BookMyEvent.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

  private final EventRepository eventRepository;
  private final UserService userService;
  private final UserRepository userRepository;
  private final EventMapper eventMapper;
  private final UserMapper userMapper;
  private final CloudinaryService mediaService;
  private final MailService mailService;
  private final ImageRepository imageRepository;
  private final UserLikedEventService likedEventService;
  private final String className = this.getClass().getSimpleName();

  @Override
  @Transactional
  public EventResponseDto createEvent(EventDTO eventDTO,
                                      MultipartFile firstImage,
                                      MultipartFile secondImage,
                                      MultipartFile thirdImage) {
    log.info("{}::createEvent - Creating new event with pending status: {}", className, eventDTO);
    if (eventDTO.getOrganizers().getId() != null) {
      List<Image> listImage = new ArrayList<>();
      try {
//        listImage = new ArrayList<>();
        try {
          List<MultipartFile> multipartFilesList = getMultipartFiles(firstImage, secondImage, thirdImage);
          String title = eventDTO.getTitle();
          listImage = mediaService.savedEventImages(multipartFilesList, title);
          log.info("{}::createEvent - saved Img and return  List of Images.", className);
        } catch (Exception e) {
          log.error("{}::createEvent - Error processing image: {}", className, e.getMessage());
          throw new GeneralException("Error processing image: " + e.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
        }
        Event event = eventMapper.toEvent(eventDTO);
        event.setCreationDate(LocalDateTime.now());
        event.setAvailableTickets(event.getNumberOfTickets());
        event.linkAllImageWithEvent(listImage);
        event.setEventStatus(EventStatus.PENDING);
        setCoordinatesToEvent(eventDTO, event);
        User user = userService.findUserById(eventDTO.getOrganizers().getId());
        event.linkUserWithEvent(user);
        Event savedEvent = eventRepository.save(event);
        userRepository.save(user);
        mailService.sendSimpleHtmlMailMessage4Line(user.getEmail(),
            "Твоя подія успішно створена на BookMyEvent",
            "Вітаємо! Твоя подія успішно створена \uD83C\uDF89",
            "Вона зараз на перевірці адміністратором. Очікуй оновлення статусу протягом 48 годин або переглядай його в розділі <strong>\"Мої події\"</strong>.",
            "Після підтвердження подія з’явиться на сайті та стане доступною для покупки всім користувачам.",
            "Дякуємо, що обрали нашу платформу! Якщо у тебе є запитання, звертайся до нашої служби підтримки.",
            ""
        );
        log.info("{}::createEvent - Event ({}) from user ({}) created successfully.", className, savedEvent.getTitle(), savedEvent.getOrganizers().getEmail());

        return eventMapper.toEventResponseDtoFromEvent(
            savedEvent,
            userMapper.toUserResponseDtoWithoutAvatarAndEvents(event.getOrganizers()));
      } catch (Exception e) {
        mediaService.deleteAllEventImg(listImage);
        throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
      }
    } else {
      throw new GeneralException("User ID can be null.", HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  @Transactional
  public EventResponseDto approveEvent(String id) {
    log.info("EventServiceImpl::approveEvent - Approving event with ID: {}", id);
    Event existingEvent = eventRepository.findById(new ObjectId(id))
        .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
    existingEvent.setEventStatus(EventStatus.APPROVED);
    Event approvedEvent = eventRepository.save(existingEvent);
    log.info("EventServiceImpl::approveEvent - Event approved successfully: {}", approvedEvent);

    return eventMapper.toEventResponseDtoFromEventWithoutUser(
        approvedEvent);
  }

  @Override
  @Transactional
  public EventResponseDto updateEvent(String id, EventDTO eventDTO, String userId) {
    log.info("EventServiceImpl::updateEvent - Updating event ID: {} with data: {}", id, eventDTO);
    Event existingEvent = eventRepository.findById(new ObjectId(id))
        .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
    if(!userId.equals(existingEvent.getOrganizers().getId().toHexString())){
      log.warn("Send error message. Authentication user id {} don't equal organizers id {} ", userId, existingEvent.getOrganizers().getId().toHexString());
      throw new GeneralException("It's not your event you can't edit it.", HttpStatus.FORBIDDEN);
    }
    eventMapper.updateEventFromDTO(eventDTO, existingEvent);
    Event updatedEvent = eventRepository.save(existingEvent);
    log.info("EventServiceImpl::updateEvent - Event updated successfully: {}", updatedEvent);
    return eventMapper.toEventResponseDtoFromEvent(updatedEvent,
        userService.findUserInfoById(updatedEvent.getOrganizers().getId().toHexString()));
  }


  @Override
  @Transactional
  public EventResponseDto updateEventImage(String id, MultipartFile eventImage) {
    log.info("EventServiceImpl::updateEventImage - Updating Img event ID: {}", id);
    Event existingEvent = eventRepository.findById(new ObjectId(id))
        .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
    Image newImage = mediaService.saveEventImage(eventImage, existingEvent.getTitle());
    existingEvent.linkImageWithEvent(newImage);
    Event updatedEvent = eventRepository.save(existingEvent);
    log.info("EventServiceImpl::updateEvent - Event updated successfully: {}", updatedEvent);

    return eventMapper.toEventResponseDtoFromEvent(updatedEvent,
        userService.findUserInfoById(updatedEvent.getOrganizers().getId().toHexString()));
  }


  @Override
  public Page<EventResponseDto> getApprovedEvents(Pageable pageable) {
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();

    Page<Event> events = eventRepository.findEventByEventStatus(EventStatus.APPROVED, pageable);
    try {
//            List<EventResponseDto> eventDTOs = events.stream()
//                .map(eventMapper::toEventResponseDtoFromEventWithoutUser)
//                .toList();
      List<EventResponseDto> sortedList = events.stream()
//                .sorted((e1, e2) -> {
//                    if (e1.getEventCategory() == EventCategory.TOP_EVENTS
//                        && e2.getEventCategory() != EventCategory.TOP_EVENTS) {
//                        return -1;
//                    } else if (e1.getEventCategory() != EventCategory.TOP_EVENTS
//                        && e2.getEventCategory() == EventCategory.TOP_EVENTS) {
//                        return 1;
//                    }
//                    return e1.getCreationDate().compareTo(e2.getCreationDate());
//                })
          .sorted(this::sortEventByCategoryTopEvents)
          .map(eventMapper::toEventResponseDtoFromEventWithoutUser)
          .toList();

      log.info("{}::{} - Found {} events", className, methodName, events.getContent().size());
      return
//                events.map(eventMapper::toEventResponseDtoFromEventWithoutUser)
          new PageImpl<>(sortedList, events.getPageable(), events.getTotalElements());
    } catch (Exception e) {

      log.info("{}}::{} - Exception  {} events", className, methodName, e.getMessage());
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public Page<EventResponseDto> getByOrganizersId(String organizerId, Pageable pageable) {
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();

    Page<Event> events = eventRepository.findByOrganizersId(new ObjectId(organizerId), pageable);
    try {
//      List<EventResponseDto> sortedList = events.stream()
//          .sorted(event-(event.getCreationDate()))
//          .map(eventMapper::toEventResponseDtoFromEventWithoutUser)
//          .toList();

      log.info("{}::{} - Found {} events", className, methodName, events.getContent().size());
//      return
//          new PageImpl<>(sortedList, events.getPageable(), events.getTotalElements());
      return events.map(eventMapper::toEventResponseDtoFromEventWithoutUser);
    } catch (Exception e) {
      log.info("{}}::{} - Exception  {} events", className, methodName, e.getMessage());
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public Page<EventResponseDto> filterEvents(EventFilterRequest filter, Pageable pageable) {

    return eventRepository.filterEvents(filter, pageable).map(eventMapper::toEventResponseDtoFromEventWithoutUser);
  }

  @Override
  public Page<EventResponseDto> getAllEvents(Pageable pageable) {
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Fetching all events", className, methodName);
    Page<Event> events = eventRepository.findAll(pageable);
    try {
      List<EventResponseDto> eventDTOs = events.stream()
//          .sorted(this::sortEventByCategoryTopEvents)
          .map(event -> {
            return eventMapper.toEventResponseDtoFromEvent(event,
                userMapper.toUserResponseDtoWithoutEvents(event.getOrganizers())
            );
          })
          .toList();

      log.info("EventServiceImpl::getEvents - Found {} events", eventDTOs.size());
      return new PageImpl<>(eventDTOs, events.getPageable(), events.getTotalElements());
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

  @Override
  @Transactional
  public void deleteEvent(String id) {
    log.info("EventServiceImpl::deleteEvent - Deleting event ID: {}", id);
    Event event = eventRepository.findById(new ObjectId(id))
        .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
    mediaService.deleteAllEventImg(event.getImages());
    eventRepository.delete(event);
    likedEventService.deleteByEventId(event.getId().toHexString());
    log.info("EventServiceImpl::deleteEvent - Event marked as deleted: {}", id);
  }

  @Override
  @Transactional
  public void deleteNotLinkedImg() {
    log.info("EventServiceImpl::deleteEvent - Deleting event img ");

    List<Event> event = eventRepository.findAll();
    List<User> userList = userRepository.findAll();
    List<Image> userImgs = userList.stream().map(
        User::getAvatarImage
    ).toList();
    List<Image> eventsImgs = event.stream().flatMap(
        event1 -> event1.getImages().stream()
    ).toList();
    List<Image> images = imageRepository.findAll();
    List<Image> imagesForDeleted = images.stream()
        .filter(img -> !eventsImgs.contains(img) && !userImgs.contains(img)).toList();
    List<String> idLs = imagesForDeleted.stream().map(img -> img.getId().toHexString()).toList();
    log.info("EventServiceImpl::deleteEvent - Img marked as deleted: size({}) imagesForDeleted : {}", imagesForDeleted.size(), idLs);

    if (imagesForDeleted.size() > 0) {
      mediaService.deleteAllEventImg(imagesForDeleted);
      log.info("EventServiceImpl::deleteEvent - Img marked as deleted:");
    }
  }

  @Override
  public EventResponseDto getEventById(String eventId) {
    log.info("EventServiceImpl::getEventById - Fetching event ID: {}", eventId);
    Event event = eventRepository.findById(new ObjectId(eventId))
        .orElseThrow(() -> new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND));

    EventResponseDto eventDTO = eventMapper.toEventResponseDtoFromEvent(event,
        userMapper.toUserResponseDto(event.getOrganizers())
    );
//        log.info("EventServiceImpl::getEventById - Found event: {}", eventDTO);
    return eventDTO;
  }

  @Override
  public EventResponseDto getApprovedEventById(String eventId) {
    log.info("EventServiceImpl::getEventById - Fetching event ID: {}", eventId);
    Event event = eventRepository.findById(new ObjectId(eventId))
        .orElseThrow(() -> new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND));
    if (event.getEventStatus().equals(EventStatus.APPROVED)) {
      EventResponseDto eventDTO = eventMapper.toEventResponseDtoFromEvent(event,
          userMapper.toUserResponseDto(event.getOrganizers())
      );
//        log.info("EventServiceImpl::getEventById - Found event: {}", eventDTO);
      return eventDTO;
    } else {
      throw new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND);
    }
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
      List<Image> imagesId = pastEvents.stream()
          .flatMap(event -> event.getImages().stream())
          .toList();
      mediaService.deleteAllEventImg(imagesId);
      eventRepository.deleteAll(pastEvents);
      pastEvents.forEach(event -> likedEventService.deleteByEventId(event.getId().toHexString()));
      log.info("EventServiceImpl::deletePastEvents - Deleted {} past events.", pastEvents.size());
    }
  }

  @Scheduled(cron = "0 0 0 * * ?")
  public void scheduledDeletePastEvents() {
    log.info("EventServiceImpl::scheduledDeletePastEvents - Running scheduled task to delete past events");
    deletePastEvents();
  }

  @Override
  @Transactional
  public EventResponseDto updateEventStatus(String id, String status, String urlToEvent) {
    log.info("EventServiceImpl::updateEventStatus - Updating event ID: {} with new status: {}", id, status);

    Event existingEvent = eventRepository.findById(new ObjectId(id))
        .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
    EventStatus newStatus = parseEventStatus(status);
    existingEvent.setEventStatus(newStatus);

    Event updatedEvent = eventRepository.save(existingEvent);
    log.info("EventServiceImpl::updateEventStatus - Event status updated successfully: {}", updatedEvent);
    if (updatedEvent.getEventStatus().equals(EventStatus.APPROVED)) {
      String activeLinks = mailService.replaceTextToLinkWithHtml(updatedEvent.getTitle(), urlToEvent);
    mailService.sendSimpleHtmlMailMessage4Line(updatedEvent.getOrganizers().getEmail(),
        "Твоя подія успішно схвалено на BookMyEvent",
        "Вітаємо! Твою подію схвалено \uD83C\uDF89",
        String.format("Твоя подія [%s] успішно пройшла перевірку та вже доступна на платформі!", activeLinks),
        "Тепер користувачі можуть переглядати її та купувати квитки. А ти заробляти. Стеж за статистикою та керуй подією в розділі <strong>\"Мої події\"</strong>.",
        "Бажаємо успішного заходу! Якщо маєш запитання, наша служба підтримки завжди на зв’язку.",
        ""
    );
  }

    return eventMapper.toEventResponseDtoFromEventWithoutUser(updatedEvent);
  }

  @Override
  public Page<EventResponseDto> getEventsByStatus(String status, Pageable pageable) {
    log.info("Fetching events with status: {}", status);

    EventStatus eventStatus;
    try {
      eventStatus = EventStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new GeneralException("Invalid status value or such status doesn't exist: " + status, HttpStatus.BAD_REQUEST);
    }
    Page<Event> events = eventRepository.findEventByEventStatus(eventStatus, pageable);

//    return events
//        .map(eventMapper::toEventResponseDtoFromEventWithoutUser)
//
//        ;

    return events.map(event ->
        eventMapper.toEventResponseDtoFromEvent(event,
            userMapper.toUserResponseDtoWithoutEvents(event.getOrganizers()))
    );
  }

//  @Override
//  @Transactional
//  public void chdb() {
//    log.info("EventServiceImpl::updateEvent - Updating event url ");
//
//    List<Event> existingEvent = eventRepository.findAll();
//
//    existingEvent.forEach(event -> {
//      if (event.getPhotoUrl() != null && !event.getPhotoUrl().isEmpty()) {
//        if (event.getImages() != null && event.getImages().size() > 0) {
//
//          event.getImages().get(0).setUrl(event.getPhotoUrl());
//          Image saveImage = imageRepository.save(event.getImages().get(0));
//
//          eventRepository.save(event);
//        } else {
//          Image newImage = Image.builder()
//              .url(event.getPhotoUrl())
//              .creationDate(LocalDateTime.now())
//              .isMain(true)
//              .name(event.getTitle())
//              .build();
//          Image saveImage = imageRepository.save(newImage);
//          event.linkImageWithEvent(saveImage);
//          eventRepository.save(event);
//        }
//      } else {
//        if (event.getImages() == null) {
//          Image newImage = Image.builder()
//              .creationDate(LocalDateTime.now())
//              .isMain(true)
//              .name(event.getTitle())
//              .build();
//          Image saveImage = imageRepository.save(newImage);
//          event.linkImageWithEvent(saveImage);
//          eventRepository.save(event);
//        }
//
//      }
//    });
//
//    log.info("EventServiceImpl::updateEventStatus - Event url updated successfully");
//  }

  @Override
  @Transactional
  public void chdbConrdinatis() {
    log.info("EventServiceImpl::chdbConrdinatis - Updating event url ");

    List<Event> existingEvent = eventRepository.findAll();

    existingEvent.forEach(event -> {
      if (event.getLocation() != null && event.getLocation().latitude() != null && event.getLocation().longitude() != null && !event.getLocation().longitude().isEmpty() && !event.getLocation().latitude().isEmpty()) {
        if (event.getCoordinates() == null) {

          event.setCoordinates(new GeoJsonPoint(
              Double.parseDouble(event.getLocation().longitude()),
              Double.parseDouble(event.getLocation().latitude()))
          );
          eventRepository.save(event);
        } else {
          log.info("EventServiceImpl::chdbConrdinatis - Updating event Conrdinatis  has chdbConrdinatis");
        }
      } else {

        log.info("EventServiceImpl::chdbConrdinatis - Updating event Conrdinatis  not has getLocation ");

      }
    });

    log.info("EventServiceImpl::updateEventStatus - Event url updated successfully");
  }

  private EventStatus parseEventStatus(String status) {
    try {
      return EventStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.error("Invalid status value: {}", status, e);
      throw new GeneralException("Invalid status value or such status doesn't exist: " + status, HttpStatus.BAD_REQUEST);
    }
  }

  public int sortEventByCategoryTopEvents(Event e1, Event e2) {
    if (e1.getEventCategory() == EventCategory.TOP_EVENTS
        && e2.getEventCategory() != EventCategory.TOP_EVENTS) {
      return -1;
    } else if (e1.getEventCategory() != EventCategory.TOP_EVENTS
        && e2.getEventCategory() == EventCategory.TOP_EVENTS) {
      return 1;
    }
    return e1.getCreationDate().compareTo(e2.getCreationDate());
  }

  private static List<MultipartFile> getMultipartFiles(MultipartFile firstImage, MultipartFile secondImage, MultipartFile thirdImage) {
    List<MultipartFile> multipartFilesList = new ArrayList<>();
    if (firstImage != null && !firstImage.isEmpty()) {
      multipartFilesList.add(firstImage);
    }
    if (secondImage != null && !secondImage.isEmpty()) {
      multipartFilesList.add(secondImage);
    }
    if (thirdImage != null && !thirdImage.isEmpty()) {
      multipartFilesList.add(thirdImage);
    }
    return multipartFilesList;
  }

  private void setCoordinatesToEvent(EventDTO eventDTO, Event event) {
    if (event.getEventFormat() != null && event.getEventFormat().equals(EventFormat.OFFLINE)
        && eventDTO.getLocation().longitude() != null
        && eventDTO.getLocation().latitude() != null
        && !eventDTO.getLocation().longitude().isEmpty()
        && !eventDTO.getLocation().latitude().isEmpty()) {
      event.setCoordinates(new GeoJsonPoint(
          Double.parseDouble(eventDTO.getLocation().longitude()),
          Double.parseDouble(eventDTO.getLocation().latitude()))
      );
      log.info("{}::setCoordinatesToEvent - add coordinates to event.", className);
    }
  }
}