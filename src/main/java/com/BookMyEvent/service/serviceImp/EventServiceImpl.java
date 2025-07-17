package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.entity.CityList;
import com.BookMyEvent.dao.EventDeleteRequestRepository;
import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.EventUpdateRequestRepository;
import com.BookMyEvent.dao.ImageRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.*;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.dto.*;
import com.BookMyEvent.exception.FieldValidationException;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.EventMapper;
import com.BookMyEvent.mapper.EventUpdateRequestMapper;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.CloudinaryService;
import com.BookMyEvent.service.EventService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.UserLikedEventService;
import com.BookMyEvent.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
  private final EventUpdateRequestMapper eventUpdateRequestMapper;
  private final CloudinaryService mediaService;
  private final MailService mailService;
  private final ImageRepository imageRepository;
  private final UserLikedEventService likedEventService;
  private final EventUpdateRequestRepository eventUpdateRepository;
  private final EventDeleteRequestRepository eventDeleteRepository;
  private final String className = this.getClass().getSimpleName();
  private final CityList cityList;



  @Override
  @Transactional
  public EventResponseDto createEvent(
      EventDTO eventDTO,
      MultipartFile firstImage,
      MultipartFile secondImage,
      MultipartFile thirdImage) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info(
        "{}::{} - Creating new event with pending status: {}", className, methodName, eventDTO);
    if (eventDTO.getOrganizers().getId() != null) {
      List<Image> listImage = new ArrayList<>();
      try {
        try {
          List<MultipartFile> multipartFilesList =
              getMultipartFiles(firstImage, secondImage, thirdImage);
          String title = eventDTO.getTitle();
          listImage = mediaService.savedEventImages(multipartFilesList, title);
          log.info("{}::{} - saved Img and return  List of Images.", className, methodName);
        } catch (Exception e) {
          log.error("{}::{} - Error processing image: {}", className, methodName, e.getMessage());
          throw new GeneralException(
              "Error processing image: " + e.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
        }
        Event event = eventMapper.toEvent(eventDTO);
        event.setCreationDate(LocalDateTime.now());
        event.setAvailableTickets(event.getNumberOfTickets());
        event.linkAllImageWithEvent(listImage);
        event.setEventStatus(EventStatus.PENDING);
        setCoordinatesToEvent(eventDTO, event);
        event.setSoldTickets(0);
        event.setProfit(BigDecimal.ZERO);
        event.setHasUpdateRequest(false);
        event.setHasCancelRequest(false);
        event.setCompleted(false);
        User user = userService.findUserById(eventDTO.getOrganizers().getId());
        event.linkUserWithEvent(user);
        Event savedEvent = eventRepository.save(event);
        userRepository.save(user);
        mailService.sendSimpleHtmlMailMessage4Line(
            user.getEmail(),
            "Твоя подія успішно створена на BookMyEvent",
            "Вітаємо! Твоя подія успішно створена \uD83C\uDF89",
            "Вона зараз на перевірці адміністратором. Очікуй оновлення статусу протягом 48 годин або переглядай його в розділі <strong>\"Мої події\"</strong>.",
            "Після підтвердження подія з’явиться на сайті та стане доступною для покупки всім користувачам.",
            "Дякуємо, що обрали нашу платформу! Якщо у тебе є запитання, звертайся до нашої служби підтримки.",
            "");
        log.info(
            "{}::{} - Event ({}) from user ({}) created successfully.",
            className,
            methodName,
            savedEvent.getTitle(),
            savedEvent.getOrganizers().getEmail());

        return eventMapper.toEventResponseDtoFromEvent(
            savedEvent, userMapper.toUserResponseDtoWithoutAvatarAndEvents(event.getOrganizers()));
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
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Approving event with ID: {}", className, methodName, id);
    Event existingEvent = findEventById(id);
    if (existingEvent.getHasUpdateRequest() || existingEvent.getHasCancelRequest()) {
      log.warn("{}::{} - Send error massage.", className, methodName);
      throw new GeneralException(
          "You can't changing status check event update or cancel requests.", HttpStatus.CONFLICT);
    }
    existingEvent.setEventStatus(EventStatus.APPROVED);
    Event approvedEvent = eventRepository.save(existingEvent);
    log.info("EventServiceImpl::approveEvent - Event approved successfully: {}", approvedEvent);

    return eventMapper.toEventResponseDtoFromEventWithoutUser(approvedEvent);
  }

  @Override
  public String createUpdateEventRequest(
      String id,
      EventUpdateDTO eventDTO,
      String userId,
      MultipartFile secondImage,
      MultipartFile thirdImage) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Updating event ID: {} with data: {}", className, methodName, id, eventDTO);

    Event existingEvent = findEventById(id);
    if (!userId.equals(existingEvent.getOrganizers().getId().toHexString())) {
      log.warn(
          "{}::{} - Send error message. Authentication user id {} don't equal organizers id {} ",
          className,
          methodName,
          userId,
          existingEvent.getOrganizers().getId().toHexString());
      throw new GeneralException("It's not your event you can't edit it.", HttpStatus.FORBIDDEN);
    }
    Optional<EventUpdateRequest> updatedEventRequest =
        eventUpdateRepository.findEventUpdateRequestByEventId(id);
    if (updatedEventRequest.isPresent()) {
      log.warn(
          "{}::{} - Send error message (The update request already exists. Wait for the administrator's response.)",
          className,
          methodName);
      throw new GeneralException(
          "The update request already exists. Wait for the administrator's response.",
          HttpStatus.CONFLICT);
    }
    Map<String, String> mapExceptionMessage =
        validatePriceNumberOfTicketsImageChanges(
            existingEvent, eventDTO.getTicketPrice(), eventDTO.getNumberOfTickets(), secondImage);
    if (mapExceptionMessage.isEmpty()) {
      EventUpdateRequest eventUpdateRequest =
          EventUpdateRequest.builder()
              .eventId(existingEvent.getId().toHexString())
              .title(eventDTO.getTitle())
              .description(eventDTO.getDescription())
              .ticketPrice(eventDTO.getTicketPrice())
              .numberOfTickets(eventDTO.getNumberOfTickets())
              .aboutOrganizer(eventDTO.getAboutOrganizer())
              .eventType(eventDTO.getEventType())
              .images(validateImageAdd(existingEvent, secondImage, thirdImage))
              .build();

      EventUpdateRequest updatedEvent = eventUpdateRepository.save(eventUpdateRequest);
      existingEvent.setEventStatus(EventStatus.PENDING);
      existingEvent.setHasUpdateRequest(true);
      eventRepository.save(existingEvent);
      log.info(
          "{}::{} - Event updated request created successfully: {}",
          className,
          methodName,
          updatedEvent);
      return "Request for update event is created successfully.";
    } else {
      log.warn(
          "{}::{} - EventServiceImpl::makeUpdateEventRequest - Field Validation Exception details: {}",
          className,
          methodName,
          mapExceptionMessage);
      throw new FieldValidationException(
          "Field Validation Exception", mapExceptionMessage, HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  @Transactional
  public EventResponseDto updateEvent(String eventId, String eventUpdateRequestId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Updating event ID: {}", className, methodName, eventId);
    Event existingEvent = findEventById(eventId);
    EventUpdateRequest updatedEventRequest =
        eventUpdateRepository
            .findById(eventUpdateRequestId)
            .orElseThrow(
                () ->
                    new GeneralException(
                        "Event not found with ID " + eventUpdateRequestId, HttpStatus.NOT_FOUND));

    Event mappedEvent =
        eventMapper.toEventFromEventUpdateRequest(existingEvent, updatedEventRequest);
    Event updatedEvent = eventRepository.save(mappedEvent);
    eventUpdateRepository.delete(updatedEventRequest);
    log.info("{}::{} - Event updated successfully: {}", className, methodName, updatedEvent);
    EventResponseDto responseDto =
        eventMapper.toEventResponseDtoFromEvent(
            updatedEvent,
            userService.findUserInfoById(updatedEvent.getOrganizers().getId().toHexString()));
    mailService.sendSimpleHtmlMailMessage4Line(
        existingEvent.getOrganizers().getEmail(),
        String.format("Зміни в події %s підтверджено.", existingEvent.getTitle()),
        "Вітаємо!",
        "Твої зміни в події успішно збережено та підтверджено.",
        "Дякуємо, що користуєшся нашим сервісом.",
        "",
        "");
    return responseDto;
  }

  @Override
  public String cancelEventUpdateRequest(String eventId, String eventUpdateRequestId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Cancel event update request event ID: {}", className, methodName, eventId);
    Event existingEvent = findEventById(eventId);
    EventUpdateRequest updatedEventRequest =
        eventUpdateRepository
            .findById(eventUpdateRequestId)
            .orElseThrow(
                () ->
                    new GeneralException(
                        "Event not found with ID " + eventUpdateRequestId, HttpStatus.NOT_FOUND));
    existingEvent.setEventStatus(EventStatus.APPROVED);
    existingEvent.setHasUpdateRequest(false);
    Event updatedEvent = eventRepository.save(existingEvent);
    eventUpdateRepository.delete(updatedEventRequest);
    mailService.sendSimpleHtmlMailMessage6Line(
        existingEvent.getOrganizers().getEmail(),
        String.format("Твоя подія [%s] поки не пройшла модерацію", existingEvent.getTitle()),
        "Привіт! \uD83D\uDC4B",
        "Дякуємо, що створюєш події разом із BookMyEvent!",
        "Але цього разу ми не можемо опублікувати зміни, які ти надіслав(-ла).",
        "Можливо, щось не відповідає нашим простим правилам. А може, просто треба трохи уточнень.",
        "Якщо хочеш розібратись або щось уточнити — пиши на:",
        " \uD83D\uDCE9 bookmyevent@gmail.com",
        " Aбо дзвони: \uD83D\uDD7D +380(99) 574 56 76");
    log.info(
        "{}::{} - Cancel event update request was successfully: {}",
        className,
        methodName,
        updatedEvent);
    return "Cancel event update request for was successfully.";
  }

  @Override
  @Transactional
  public EventUpdateRequestDTO getEventUpdateRequestById(String eventId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    EventUpdateRequest updatedEventRequest =
        eventUpdateRepository
            .findEventUpdateRequestByEventId(eventId)
            .orElseThrow(
                () ->
                    new GeneralException(
                        "Event not found with ID " + eventId, HttpStatus.NOT_FOUND));
    log.info("{}::{} - fined eventUpdateRequest by  event id: {}", className, methodName, eventId);
    return new EventUpdateRequestDTO(
        updatedEventRequest.getId(),
        updatedEventRequest.getEventId(),
        updatedEventRequest.getTitle(),
        updatedEventRequest.getDescription(),
        updatedEventRequest.getTicketPrice(),
        updatedEventRequest.getUnlimitedTickets(),
        updatedEventRequest.getNumberOfTickets(),
        updatedEventRequest.getAboutOrganizer(),
        updatedEventRequest.getEventType().getUkrainianName(),
        updatedEventRequest.getEventCategory(),
        updatedEventRequest.getEventStatus(),
        updatedEventRequest.getImages());
  }

  @Override
  @Transactional
  public EventDeleteRequest getEventCancelRequestById(String eventId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    EventDeleteRequest cancelEventRequest =
        eventDeleteRepository
            .findByEventId(eventId)
            .orElseThrow(
                () ->
                    new GeneralException(
                        "Event not found with ID " + eventId, HttpStatus.NOT_FOUND));
    log.info("{}::{} - fined eventUpdateRequest by  event id: {}", className, methodName, eventId);
    return cancelEventRequest;
  }

  @Override
  @Transactional
  public EventResponseDto updateEventImage(String eventId, MultipartFile eventImage) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Updating Img event ID: {}", className, methodName, eventId);
    Event existingEvent = findEventById(eventId);
    Image newImage = mediaService.saveEventImage(eventImage, existingEvent.getTitle());
    existingEvent.linkImageWithEvent(newImage);
    Event updatedEvent = eventRepository.save(existingEvent);
    log.info("{}::{} - Event updated successfully: {}", className, methodName, updatedEvent);

    return eventMapper.toEventResponseDtoFromEvent(
        updatedEvent,
        userService.findUserInfoById(updatedEvent.getOrganizers().getId().toHexString()));
  }

  @Override
  public Page<EventResponseDto> getApprovedEvents(Pageable pageable, String city) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    //    Page<Event> events = eventRepository.findEventByEventStatus(EventStatus.APPROVED, city,
    // pageable);
    log.info("{}::{} - events with filter city ({}).", className, methodName, city);
    if (city == null) {
      Page<Event> events = eventRepository.findEventByEventStatus(EventStatus.APPROVED, pageable);
      if (events.getContent().isEmpty()) {
        return new PageImpl<>(List.of(), events.getPageable(), events.getTotalElements());
      }
      log.info(
          "{}::{} - Found {} events without filter city.",
          className,
          methodName,
          events.getTotalElements());

      return getPageSortedByCategoryTopEvents(events);
    } else if (cityList.getCityList().contains(city)) {
      Page<Event> events =
          eventRepository.findEventByEventStatusAndLocation_City(
              EventStatus.APPROVED, city, pageable);
      //      if (events.getContent().isEmpty()) {
      //        return new PageImpl<>(List.of(), events.getPageable(), events.getTotalElements());
      //      }
      log.info(
          "{}::{} - Found {} events with filter city ({}).",
          className,
          methodName,
          events.getTotalElements(),
          city);

      return getPageSortedByCategoryTopEvents(events);
    } else {
      log.error("{}::{} - Return error message.", className, methodName);
      throw new GeneralException("The city name was entered incorrectly.", HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public List<EventResponseDto> getTopEvents(Integer size) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    List<Event> events = eventRepository.findRandomEventsByCategory(EventCategory.TOP_EVENTS, EventStatus.APPROVED, size);
    try {
      log.info("{}::{} - Found {} events", className, methodName, events.size());
      return events.stream().map(eventMapper::toEventResponseDtoFromEventWithoutUser).toList();
    } catch (Exception e) {
      log.error("{}::{} - Return error message.", className, methodName);
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public List<EventResponseDto> getNewEvents(Integer size, String cityName) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime toDate = LocalDateTime.now();
    LocalDateTime fromDate= toDate.minusDays(7);
    List<Event> events = new ArrayList<>();
    if (cityName == null) {
      events =
          eventRepository.findRandomEventsByCreationDate(
              fromDate, toDate, EventStatus.APPROVED, size);
      log.info("{}::{} - Found {} events", className, methodName, events.size());
    } else if (cityList.getCityList().contains(cityName)) {
      events =
          eventRepository.findRandomEventsByCreationDateByCity(
              fromDate, toDate, cityName, EventStatus.APPROVED, size);
      log.info("{}::{} - Found {} events", className, methodName, events.size());
    } else {
      log.error("{}::{} - Return error message.", className, methodName);
      throw new GeneralException("The city name was entered incorrectly.", HttpStatus.BAD_REQUEST);
    }
    try {
      log.info("{}::{} - Found {} events", className, methodName, events.size());
      return events.stream().map(eventMapper::toEventResponseDtoFromEventWithoutUser).toList();
    } catch (Exception e) {
      log.warn("{}::{} - Return error message.", className, methodName);
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public Page<EventResponseDto> getByOrganizersId(String organizerId, Pageable pageable) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    Page<Event> events = eventRepository.findByOrganizersId(new ObjectId(organizerId), pageable);
    try {
      log.info("{}::{} - Found {} events", className, methodName, events.getContent().size());

      return events.map(eventMapper::toEventResponseDtoFromEventWithoutUser);
    } catch (Exception e) {
      log.info("{}::{} - Exception  {} events", className, methodName, e.getMessage());
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public Page<EventResponseDto> filterEvents(EventFilterRequest filter, Pageable pageable) {
    return eventRepository
        .filterEvents(filter, pageable)
        .map(eventMapper::toEventResponseDtoFromEventWithoutUser);
  }

  @Override
  public Page<EventResponseDto> getAllEvents(Pageable pageable) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Fetching all events", className, methodName);
    Page<Event> events = eventRepository.findAll(pageable);
    try {
      List<EventResponseDto> eventDTOs =
          events.stream()
              //          .sorted(this::sortEventByCategoryTopEvents)
              .map(
                  event -> {
                    //            EventUpdateRequest eventUpdateRequest = null;
                    //            EventCancelRequest eventCancelRequest = null;
                    //            if (event.getHasUpdateRequest()) {
                    //              eventUpdateRequest =
                    // eventUpdateRepository.findEventUpdateRequestByEventId(event.getId().toHexString())
                    //                  .orElse(new EventUpdateRequest());
                    //            }
                    //            if (event.getHasUpdateRequest()) {
                    //              eventCancelRequest =
                    // eventCancelRepository.findEventCancelRequestByEventId(event.getId().toHexString())
                    //                  .orElse(new EventCancelRequest());
                    //            }
                    return eventMapper.toEventResponseDtoFromEvent(
                        event, userMapper.toUserResponseDtoWithoutEvents(event.getOrganizers())
                        //                eventUpdateRequest,
                        //                eventCancelRequest
                        );
                  })
              .toList();

      log.info("{}::{} - Found {} events", className, methodName, eventDTOs.size());
      return new PageImpl<>(eventDTOs, events.getPageable(), events.getTotalElements());
    } catch (Exception e) {
      log.info("{}::{} - Exception  {} events", className, methodName, e.getMessage());
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public Map<String, Integer> countByStatus() {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Start counting events for all statuses", className, methodName);
    Map<String, Integer> statusCountMap =
        Arrays.stream(EventStatus.values())
            .collect(
                Collectors.toMap(
                    Enum::toString,
                    status -> eventRepository.findEventByEventStatus(status).size()));
    log.info("{}::{} - Events count by status: {}", className, methodName, statusCountMap);
    return statusCountMap;
  }

  @Override
  @Transactional
  public String createDeleteEventRequest(
      String eventId, EventDeleteRequest eventDeleteRequest, String userId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Deleting request event ID: {}", className, methodName, eventId);
    Event existingEvent = findEventById(eventId);
    if (!userId.equals(existingEvent.getOrganizers().getId().toHexString())) {
      log.warn(
          "{}::{} - Send error message. Authentication user id {} don't equal organizers id {} ",
          className,
          methodName,
          userId,
          existingEvent.getOrganizers().getId().toHexString());
      throw new GeneralException("It's not your event you can't edit it.", HttpStatus.FORBIDDEN);
    }
    //    Optional<EventCancelRequest> existCancelRequest =
    // eventCancelRepository.findEventCancelRequestByEventId(eventId);
    if (eventDeleteRepository.existsByEventId(eventId)) {
      log.warn("{}::{} - Send error message.", className, methodName);
      throw new GeneralException(
          "The cancel request already exists. Wait for the administrator's response.",
          HttpStatus.CONFLICT);
    }
    EventDeleteRequest newDeleteRequest =
        new EventDeleteRequest(
            eventId, userId, eventDeleteRequest.getContact(), eventDeleteRequest.getReason());
    EventDeleteRequest savedCancelRequest = eventDeleteRepository.save(newDeleteRequest);
    existingEvent.setHasCancelRequest(true);
    existingEvent.setEventStatus(EventStatus.PENDING);
    eventRepository.save(existingEvent);
    log.info(
        "{}::{} - Event updated request created successfully: {}",
        className,
        methodName,
        savedCancelRequest);

    return "Request for canceling event is created successfully.";
  }

  @Override
  @Transactional
  public void deleteEvent(String eventId, String eventDeleteRequestId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Deleting event ID: {}", className, methodName, eventId);
    Event event = findEventById(eventId);
    EventDeleteRequest existDeleteRequest =
        eventDeleteRepository
            .findByEventId(eventId)
            .orElseThrow(
                () ->
                    new GeneralException(
                        "Event delete request not found with ID " + eventId, HttpStatus.NOT_FOUND));
    if (!Objects.equals(existDeleteRequest.getId(), eventDeleteRequestId)) {
      log.warn("{}::{} - Send error message.", className, methodName);
      throw new GeneralException("Invalid event delete request id.", HttpStatus.FORBIDDEN);
    }
    mediaService.deleteAllEventImg(event.getImages());
    eventRepository.delete(event);
    eventDeleteRepository.delete(existDeleteRequest);
    likedEventService.deleteByEventId(event.getId().toHexString());
    log.info("{}::{} - Event marked as deleted: {}", className, methodName, eventId);
  }

  @Override
  public void userDeleteEvent(String eventId, String userId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Deleting event ID: {}", className, methodName, eventId);
    Event event = findEventById(eventId);
    if (hasEventSales(event)) {
      log.warn("{}::{} - Send error message.", className, methodName);
      throw new GeneralException(
          "You can't delete event. Make a cancel request to the admin.", HttpStatus.FORBIDDEN);
    }
    if (!Objects.equals(event.getOrganizers().getId().toHexString(), userId)) {
      log.warn("{}::{} - Send error message.", className, methodName);
      throw new GeneralException(
          "You can't delete event. You are not the organizer.", HttpStatus.FORBIDDEN);
    }
    mediaService.deleteAllEventImg(event.getImages());
    eventRepository.delete(event);
    likedEventService.deleteByEventId(event.getId().toHexString());
    log.info("{}::{} - Event marked as deleted: {}", className, methodName, eventId);
  }

  @Override
  @Transactional
  public void deleteNotLinkedImg() {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Deleting event img.", className, methodName);
    List<Event> event = eventRepository.findAll();
    List<User> userList = userRepository.findAll();
    List<Image> userImgs = userList.stream().map(User::getAvatarImage).toList();
    List<Image> eventsImgs = event.stream().flatMap(event1 -> event1.getImages().stream()).toList();
    List<Image> images = imageRepository.findAll();
    List<Image> imagesForDeleted =
        images.stream()
            .filter(img -> !eventsImgs.contains(img) && !userImgs.contains(img))
            .toList();
    List<String> idLs = imagesForDeleted.stream().map(img -> img.getId().toHexString()).toList();
    log.info(
        "{}::{} - Img marked as deleted: size({}) imagesForDeleted : {}",
        className,
        methodName,
        imagesForDeleted.size(),
        idLs);

    if (imagesForDeleted.size() > 0) {
      mediaService.deleteAllEventImg(imagesForDeleted);
      log.info("{}::{} - Img marked as deleted", className, methodName);
    }
  }

  @Override
  public EventResponseDto getEventById(String eventId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Fetching event ID: {}", className, methodName, eventId);
    Event event = findEventById(eventId);
    EventResponseDto eventDTO =
        eventMapper.toEventResponseDtoFromEvent(
            event, userMapper.toUserResponseDto(event.getOrganizers()));
    log.info("{}::{} - Found event by id: {}", className, methodName, eventDTO.getId());
    return eventDTO;
  }

  @Override
  public EventResponseDto getApprovedEventById(String eventId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Fetching event ID: {}", className, methodName, eventId);
    Event event = findEventById(eventId);
    if (event.getEventStatus().equals(EventStatus.APPROVED)) {
      EventResponseDto eventDTO =
          eventMapper.toEventResponseDtoFromEvent(
              event, userMapper.toUserResponseDto(event.getOrganizers()));
      log.info("{}::{} - Found event by id: {}", className, methodName, eventDTO.getId());
      return eventDTO;
    } else {
      log.warn("{}::{} - Send error message.", className, methodName);
      throw new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public void deletePastEvents() {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Deleting past events...", className, methodName);
    LocalDate now = LocalDate.now().minusDays(1);
    List<Event> pastEvents = eventRepository.findByDateDay(now.toString());
    if (pastEvents.isEmpty()) {
      log.info("{}::{} - No past events found for deletion.", className, methodName);
    } else {
      List<Image> imagesId =
          pastEvents.stream().flatMap(event -> event.getImages().stream()).toList();
      mediaService.deleteAllEventImg(imagesId);
      eventRepository.deleteAll(pastEvents);
      pastEvents.forEach(event -> likedEventService.deleteByEventId(event.getId().toHexString()));
      log.info("{}::{} - Deleted {} past events.", className, methodName, pastEvents.size());
    }
  }

  @Scheduled(cron = "0 0 0 * * ?")
  public void scheduledDeletePastEvents() {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Running scheduled task to delete past events", className, methodName);
    deletePastEvents();
  }

  @Override
  @Transactional
  public EventResponseDto updateEventStatus(String id, String status, String urlToEvent) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info(
        "{}::{} - Updating event ID: {} with new status: {}", className, methodName, id, status);
    Event existingEvent = findEventById(id);
    if (existingEvent.getHasUpdateRequest() || existingEvent.getHasCancelRequest()) {
      log.warn("{}::{} - Send error massage.", className, methodName);
      throw new GeneralException(
          "You can't changing status check event update or cancel requests.", HttpStatus.CONFLICT);
    }
    EventStatus newStatus = parseEventStatus(status);
    existingEvent.setEventStatus(newStatus);

    Event updatedEvent = eventRepository.save(existingEvent);
    log.info("{}::{} - Event status updated successfully: {}", className, methodName, updatedEvent);
    if (updatedEvent.getEventStatus().equals(EventStatus.APPROVED)) {
      String activeLinks =
          mailService.replaceTextToLinkWithHtml(updatedEvent.getTitle(), urlToEvent);
      mailService.sendSimpleHtmlMailMessage4Line(
          updatedEvent.getOrganizers().getEmail(),
          "Твоя подія успішно схвалена на BookMyEvent",
          "Вітаємо! Твою подію схвалено \uD83C\uDF89",
          String.format(
              "Твоя подія [%s] успішно пройшла перевірку та вже доступна на платформі!",
              activeLinks),
          "Тепер користувачі можуть переглядати її та купувати квитки. А ти заробляти. Стеж за статистикою та керуй подією в розділі <strong>\"Мої події\"</strong>.",
          "Бажаємо успішного заходу! Якщо маєш запитання, наша служба підтримки завжди на зв’язку.",
          "");
    }
    if (updatedEvent.getEventStatus().equals(EventStatus.CANCELLED)) {
      mailService.sendSimpleHtmlMailMessage6Line(
          existingEvent.getOrganizers().getEmail(),
          String.format("Твоя подія [%s] поки не пройшла модерацію", existingEvent.getTitle()),
          "Привіт! \uD83D\uDC4B",
          "Дякуємо, що створюєш події разом із BookMyEvent!",
          "Але цього разу ми не можемо опублікувати подію, яку ти надіслав(-ла).",
          "Можливо, щось не відповідає нашим простим правилам. А може, просто треба трохи уточнень.",
          "Якщо хочеш розібратись або щось уточнити — пиши на:",
          " \uD83D\uDCE9 bookmyevent@gmail.com",
          " Aбо дзвони: \uD83D\uDD7D +380(99) 574 56 76");
    }

    return eventMapper.toEventResponseDtoFromEventWithoutUser(updatedEvent);
  }

  @Override
  public Page<EventResponseDto> getEventsByStatus(String status, Pageable pageable) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Fetching events with status: {}", className, methodName, status);
    EventStatus eventStatus;
    try {
      eventStatus = EventStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new GeneralException(
          "Invalid status value or such status doesn't exist: " + status, HttpStatus.BAD_REQUEST);
    }
    Page<Event> events = eventRepository.findEventByEventStatus(eventStatus, pageable);
    return events.map(
        event ->
            eventMapper.toEventResponseDtoFromEvent(
                event, userMapper.toUserResponseDtoWithoutEvents(event.getOrganizers())));
  }

  private EventStatus parseEventStatus(String status) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    try {
      return EventStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("{}::{} Invalid status value: {}", className, methodName, status, e);
      throw new GeneralException(
          "Invalid status value or such status doesn't exist: " + status, HttpStatus.BAD_REQUEST);
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

  private static List<MultipartFile> getMultipartFiles(
      MultipartFile firstImage, MultipartFile secondImage, MultipartFile thirdImage) {
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
    if (event.getEventFormat() != null
        && event.getEventFormat().equals(EventFormat.OFFLINE)
        && eventDTO.getLocation().longitude() != null
        && eventDTO.getLocation().latitude() != null
        && !eventDTO.getLocation().longitude().isEmpty()
        && !eventDTO.getLocation().latitude().isEmpty()) {
      event.setCoordinates(
          new GeoJsonPoint(
              Double.parseDouble(eventDTO.getLocation().longitude()),
              Double.parseDouble(eventDTO.getLocation().latitude())));
      log.info("{}::setCoordinatesToEvent - add coordinates to event.", className);
    }
  }

  private Map<String, String> validatePriceNumberOfTicketsImageChanges(
      Event existingEvent,
      Long newTicketPrice,
      Integer newNumberOfTickets,
      MultipartFile secondImage) {
    Map<String, String> mapExceptionMessage = new HashMap<>();
    if (Optional.ofNullable(existingEvent.getSoldTickets()).orElse(0) > 0
        && !Objects.equals(
            existingEvent.getNumberOfTickets(), existingEvent.getAvailableTickets())) {
      if (newTicketPrice != null) {
        mapExceptionMessage.put("ticketPrice", "The ticket price can no longer be changed.");
      }
      if (newNumberOfTickets != null) {
        mapExceptionMessage.put(
            "numberOfTickets", "The number of tickets can no longer be changed.");
      }
    }
    if (existingEvent.getImages().size() == 3) {
      mapExceptionMessage.put(
          "images", "All photos have already been added, you cannot change or add.");
    }
    if (existingEvent.getImages().size() == 2) {
      if (secondImage != null) {
        mapExceptionMessage.put("images", "You cannot change or add second Image.");
      }
    }
    return mapExceptionMessage;
  }

  private Long validatePriceChange(Event existingEvent, Long newTicketPrice) {
    if (Optional.ofNullable(existingEvent.getSoldTickets()).orElse(0) <= 0
        && Objects.equals(
            existingEvent.getNumberOfTickets(), existingEvent.getAvailableTickets())) {
      return newTicketPrice;
    }
    return existingEvent.getTicketPrice();
  }

  private boolean hasEventSales(Event existingEvent) {
    boolean hasEventSales =
        Optional.ofNullable(existingEvent.getSoldTickets()).orElse(0) > 0
            || !Objects.equals(
                existingEvent.getNumberOfTickets(), existingEvent.getAvailableTickets());

    return hasEventSales;
  }

  private List<Image> validateImageAdd(
      Event existingEvent, MultipartFile secondImage, MultipartFile thirdImage) {
    log.info("EventServiceImpl::validateImageAdd - Updating event image");
    List<Image> list = new ArrayList<>();
    if (existingEvent.getImages().size() == 3) {
      return existingEvent.getImages();
    }
    if (existingEvent.getImages().size() == 1) {
      list.addAll(existingEvent.getImages());
      if (secondImage != null) {
        Image newImage2 = mediaService.saveEventImage(secondImage, existingEvent.getTitle());
        list.add(newImage2);
      }
      if (thirdImage != null) {
        Image newImage = mediaService.saveEventImage(thirdImage, existingEvent.getTitle());
        list.addAll(existingEvent.getImages());
        list.add(newImage);
      }
      return list;
    }
    if (existingEvent.getImages().size() == 2) {
      if (thirdImage != null) {
        Image newImage = mediaService.saveEventImage(thirdImage, existingEvent.getTitle());
        list.addAll(existingEvent.getImages());
        list.add(newImage);
        return list;
      }
    }
    return existingEvent.getImages();
  }

  private Event findEventById(String eventId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    log.info("{}::{} - Try get exist event from DB. Id: {}", className, methodName, eventId);
    return eventRepository
        .findById(new ObjectId(eventId))
        .orElseThrow(
            () -> new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND));
  }

  private PageImpl<EventResponseDto> getPageSortedByCategoryTopEvents(Page<Event> events) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    try {
      List<EventResponseDto> sortedList =
          events.stream()
              .sorted(this::sortEventByCategoryTopEvents)
              .map(eventMapper::toEventResponseDtoFromEventWithoutUser)
              .toList();
      log.info("{}::{} - Found {} events", className, methodName, events.getContent().size());

      return new PageImpl<>(sortedList, events.getPageable(), events.getTotalElements());
    } catch (Exception e) {
      log.error("{}::{} - Return error message.", className, methodName);
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}
