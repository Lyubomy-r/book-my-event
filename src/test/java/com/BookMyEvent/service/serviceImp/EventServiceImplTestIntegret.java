package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventDeleteRequestRepository;
import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.EventUpdateRequestRepository;
import com.BookMyEvent.dao.ImageRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.EventDeleteRequest;
import com.BookMyEvent.entity.EventUpdateRequest;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.EventUpdateDTO;
import com.BookMyEvent.entity.dto.EventUpdateRequestDTO;
import com.BookMyEvent.exception.FieldValidationException;
import com.BookMyEvent.service.MailService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class EventServiceImplTestIntegret {

  @Container
  private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

  @DynamicPropertySource
  static void mongoDbProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired private EventServiceImpl eventServiceImpl;
  @Autowired private EventRepository eventRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EventUpdateRequestRepository eventUpdateRepository;
  @Autowired private EventDeleteRequestRepository eventCancelRepository;
  @Autowired private ImageRepository imageRepository;
  @MockBean private MailService mailService;

  private Event event;
  private DateDetails dateDetails;
  private EventResponseDto eventResponseDto;
  private User userOne;
  DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("uk"));
  DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

  @BeforeEach
  void setUp() {
    eventRepository.deleteAll();
    userRepository.deleteAll();
    eventUpdateRepository.deleteAll();
    eventCancelRepository.deleteAll();
    LocalTime localTime = LocalTime.now();
    List<Image> imageList =
        Arrays.asList(Image.builder().id(new ObjectId("66c648b600179737a3d5c212")).build());

    userOne =
        User.builder()
            .id(new ObjectId("66c648b600179737a3d5c235"))
            .name("Ronald")
            .email("sewewt@code.com")
            .password("As123ertyuer")
            .location("Kyiv")
            .mailConfirmation(true)
            .status(Status.ACTIVE)
            .role(Role.VISITOR)
            .creationDate(LocalDateTime.now())
            .build();

    event = new Event();
    event.setId(new ObjectId("66c648b600179737a3d5c235"));
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS);
    event.setEventCategory(EventCategory.TOP_EVENTS);
    event.setEventStatus(EventStatus.PENDING);
    event.setEventFormat(EventFormat.OFFLINE);
    event.setAvailableTickets(100);
    event.setNumberOfTickets(100);
    event.setUnlimitedTickets(false);
    event.setPhoneNumber("+380961232456");
    event.setTicketPrice(800L);
    event.setLocation(new Location("Київ", "вул. Успішна, 1", "", "50.426129", "30.514067"));
    event.setAboutOrganizer("Text About Organizer");
    event.setRating(4.2D);
    event.setImages(imageList);
    //    event.setOrganizers(User.builder().id(new ObjectId("66c648b600179737a3d5c211"))
    //        .email("test@email.com")
    //        .createdEvents(new ArrayList<>())
    //        .build());
    event.setOrganizers(userOne);
    event.setDate(
        new DateDetails(
            LocalDate.of(LocalDate.now().plusYears(1).getYear(), 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
    event.setHasUpdateRequest(false);
    event.setHasCancelRequest(false);

    userRepository.save(userOne);
    eventRepository.save(event);

    dateDetails =
        new DateDetails(
            LocalDate.parse(event.getDate().day()).format(dayFormatter),
            LocalTime.parse(event.getDate().time()).format(timeFormatter),
            LocalTime.parse(event.getDate().endTime()).format(timeFormatter));

    eventResponseDto = new EventResponseDto();
    eventResponseDto.setId(event.getId().toHexString());
    eventResponseDto.setTitle(event.getTitle());
    eventResponseDto.setDescription(event.getDescription());
    eventResponseDto.setEventType(event.getEventType().getUkrainianName());
    eventResponseDto.setEventCategory(event.getEventCategory().toString());
    eventResponseDto.setEventStatus(event.getEventStatus().toString());
    eventResponseDto.setEventFormat(event.getEventFormat().toString());
    eventResponseDto.setAvailableTickets(event.getAvailableTickets());
    eventResponseDto.setNumberOfTickets(event.getNumberOfTickets());
    eventResponseDto.setUnlimitedTickets(event.getUnlimitedTickets());
    eventResponseDto.setPhoneNumber(event.getPhoneNumber());
    eventResponseDto.setTicketPrice(event.getTicketPrice());
    eventResponseDto.setLocation(event.getLocation());
    eventResponseDto.setAboutOrganizer(event.getAboutOrganizer());
    eventResponseDto.setRating(event.getRating());
    eventResponseDto.setImages(event.getImages());
    eventResponseDto.setDate(event.getDate());
  }

  @Test
  void connectionEstablished() {
    assertThat(mongoDBContainer.isCreated()).isTrue();
    assertThat(mongoDBContainer.isRunning()).isTrue();
  }

  @Test
  public void makeUpdateEventRequest() {

    EventUpdateDTO eventDTO = new EventUpdateDTO();
    eventDTO.setTitle("new Title");
    eventDTO.setDescription("Description about event info");
    eventDTO.setTicketPrice(500L);
    eventDTO.setNumberOfTickets(2000);
    eventDTO.setAboutOrganizer(event.getAboutOrganizer());
    eventDTO.setEventType(EventType.OTHER);

    String expectedMessage = "Request for update event is created successfully.";
    String responseMessage = helpMethodMakeUpdateEventRequest(eventDTO);
    Optional<EventUpdateRequest> existingUpdateRequest =
        eventUpdateRepository.findEventUpdateRequestByEventId(event.getId().toHexString());
    Optional<Event> existingEvent = eventRepository.findById(event.getId());

    assertEquals(expectedMessage, responseMessage);
    assertTrue(existingUpdateRequest.isPresent());
    assertEquals(event.getId().toHexString(), existingUpdateRequest.get().getEventId());
    assertEquals(eventDTO.getTitle(), existingUpdateRequest.get().getTitle());
    assertTrue(existingEvent.isPresent());
    assertNotEquals(existingEvent.get().getTitle(), existingUpdateRequest.get().getTitle());
    assertTrue(existingEvent.get().getHasUpdateRequest());
    assertFalse(existingEvent.get().getHasCancelRequest());
  }

  @Test
  public void makeUpdateEventRequestNegativeScenarioTicketPrice() {
    event.setSoldTickets(1);
    event.setAvailableTickets(event.getAvailableTickets() - 1);
    eventRepository.save(event);
    EventUpdateDTO eventDTO = new EventUpdateDTO();
    eventDTO.setTitle("new Title");
    eventDTO.setDescription("Description about event info");
    eventDTO.setTicketPrice(500L);
    eventDTO.setNumberOfTickets(2000);
    eventDTO.setAboutOrganizer(event.getAboutOrganizer());
    eventDTO.setEventType(EventType.OTHER);
    String expectedMessage = "The ticket price can no longer be changed.";

    FieldValidationException fieldValidationException =
        assertThrows(
            FieldValidationException.class,
            () ->
                eventServiceImpl.createUpdateEventRequest(
                    event.getId().toHexString(),
                    eventDTO,
                    userOne.getId().toHexString(),
                    null,
                    null));
    Optional<EventUpdateRequest> existingUpdateRequest =
        eventUpdateRepository.findEventUpdateRequestByEventId(event.getId().toHexString());
    Optional<Event> existingEvent = eventRepository.findById(event.getId());

    assertEquals(expectedMessage, fieldValidationException.getDetails().get("ticketPrice"));
    assertFalse(existingUpdateRequest.isPresent());
    assertTrue(existingEvent.isPresent());
    assertFalse(existingEvent.get().getHasUpdateRequest());
    assertFalse(existingEvent.get().getHasCancelRequest());
  }

  @Test
  public void makeUpdateEventRequestNegativeScenarioNumberOfTickets() {
    event.setSoldTickets(1);
    event.setAvailableTickets(event.getAvailableTickets() - 1);
    eventRepository.save(event);
    EventUpdateDTO eventDTO = new EventUpdateDTO();
    eventDTO.setTitle("new Title");
    eventDTO.setDescription("Description about event info");
    eventDTO.setNumberOfTickets(2000);
    eventDTO.setAboutOrganizer(event.getAboutOrganizer());
    eventDTO.setEventType(EventType.OTHER);
    String expectedMessage = "The number of tickets can no longer be changed.";

    FieldValidationException fieldValidationException =
        assertThrows(
            FieldValidationException.class,
            () ->
                eventServiceImpl.createUpdateEventRequest(
                    event.getId().toHexString(),
                    eventDTO,
                    userOne.getId().toHexString(),
                    null,
                    null));
    Optional<EventUpdateRequest> existingUpdateRequest =
        eventUpdateRepository.findEventUpdateRequestByEventId(event.getId().toHexString());
    Optional<Event> existingEvent = eventRepository.findById(event.getId());

    assertEquals(expectedMessage, fieldValidationException.getDetails().get("numberOfTickets"));
    assertFalse(existingUpdateRequest.isPresent());
    assertTrue(existingEvent.isPresent());
    assertFalse(existingEvent.get().getHasUpdateRequest());
    assertFalse(existingEvent.get().getHasCancelRequest());
  }

  @Test
  void updateEvent() {
    EventUpdateRequest eventUpdateRequest =
        EventUpdateRequest.builder()
            .id("66c648b600179737a3d5c277")
            .eventId(event.getId().toHexString())
            .title("new Title")
            .description("Description about event info")
            .ticketPrice(500L)
            .numberOfTickets(2000)
            .aboutOrganizer(event.getAboutOrganizer())
            .eventType(EventType.OTHER)
            .images(null)
            .build();
    event.setHasUpdateRequest(true);
    eventRepository.save(event);
    eventUpdateRepository.save(eventUpdateRequest);
    doNothing()
        .when(mailService)
        .sendSimpleHtmlMailMessage4Line(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString());

    EventResponseDto updateEvent =
        eventServiceImpl.updateEvent(event.getId().toHexString(), eventUpdateRequest.getId());
    Optional<Event> existingEvent = eventRepository.findById(event.getId());

    assertNotNull(updateEvent);
    assertEquals(event.getId().toHexString(), updateEvent.getId());
    assertEquals(eventUpdateRequest.getTitle(), updateEvent.getTitle());
    assertEquals(eventUpdateRequest.getDescription(), updateEvent.getDescription());
    assertEquals(eventUpdateRequest.getTicketPrice(), updateEvent.getTicketPrice());
    assertEquals(eventUpdateRequest.getNumberOfTickets(), updateEvent.getNumberOfTickets());
    assertEquals(eventUpdateRequest.getEventType().getUkrainianName(), updateEvent.getEventType());
    assertFalse(updateEvent.getHasUpdateRequest());
    assertFalse(updateEvent.getHasCancelRequest());
    assertFalse(existingEvent.get().getHasUpdateRequest());
    assertFalse(existingEvent.get().getHasCancelRequest());

    verify(mailService, times(1))
        .sendSimpleHtmlMailMessage4Line(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString());
  }

  @Test
  void getEventUpdateRequestById() {
    EventUpdateRequest eventUpdateRequest =
        EventUpdateRequest.builder()
            .id("66c648b600179737a3d5c277")
            .eventId(event.getId().toHexString())
            .title("new Title")
            .description("Description about event info")
            .ticketPrice(500L)
            .numberOfTickets(2000)
            .aboutOrganizer(event.getAboutOrganizer())
            .eventType(EventType.OTHER)
            .images(null)
            .build();
    eventUpdateRepository.save(eventUpdateRequest);

    EventUpdateRequestDTO existedUpdateRequest =
        eventServiceImpl.getEventUpdateRequestById(eventUpdateRequest.getEventId());

    assertNotNull(existedUpdateRequest);
    assertEquals(eventUpdateRequest.getId(), existedUpdateRequest.id());
    assertEquals(eventUpdateRequest.getTitle(), existedUpdateRequest.title());
    assertEquals(
        eventUpdateRequest.getEventType().getUkrainianName(), existedUpdateRequest.eventType());
  }

  @Test
  void getEventCancelRequestById() {}

  @Test
  void getAllEvents() {
    Pageable pageable = PageRequest.of(0, 8);
    Page<User> eventPage = new PageImpl<>(List.of(userOne), pageable, 1);
    EventUpdateDTO eventDTO = new EventUpdateDTO();
    eventDTO.setTitle("new Title");
    eventDTO.setDescription("Description about event info");
    eventDTO.setTicketPrice(500L);
    eventDTO.setNumberOfTickets(2000);
    eventDTO.setAboutOrganizer(event.getAboutOrganizer());
    eventDTO.setEventType(EventType.OTHER);
    String message = helpMethodMakeUpdateEventRequest(eventDTO);

    Page<EventResponseDto> eventResponseDtoPage = eventServiceImpl.getAllEvents(pageable);

    assertFalse(eventResponseDtoPage.isEmpty());
    assertEquals(1, eventResponseDtoPage.getContent().size());
    assertTrue(eventResponseDtoPage.getContent().get(0).getHasUpdateRequest());
    assertFalse(eventResponseDtoPage.getContent().get(0).getHasCancelRequest());
  }

  @Test
  public void createDeleteEventRequest() {
    eventRepository.save(event);
    String reasonMessage = "Some reasons";
    String contact = "0923456326";
    EventDeleteRequest eventDTO = new EventDeleteRequest();
    eventDTO.setContact(contact);
    eventDTO.setReason(reasonMessage);
    String expectedMessage = "Request for canceling event is created successfully.";

    String responseMessage =
        eventServiceImpl.createDeleteEventRequest(
            event.getId().toHexString(), eventDTO, userOne.getId().toHexString());
    Optional<EventDeleteRequest> existingDeleteRequest =
        eventCancelRepository.findByEventId(event.getId().toHexString());
    Optional<Event> existingEvent = eventRepository.findById(event.getId());

    assertEquals(expectedMessage, responseMessage);
    assertTrue(existingDeleteRequest.isPresent());
    assertEquals(reasonMessage, existingDeleteRequest.get().getReason());
    assertEquals(contact, existingDeleteRequest.get().getContact());
    assertEquals(event.getId().toHexString(), existingDeleteRequest.get().getEventId());
    assertTrue(existingEvent.isPresent());
    assertFalse(existingEvent.get().getHasUpdateRequest());
    assertTrue(existingEvent.get().getHasCancelRequest());
    assertEquals(EventStatus.PENDING, existingEvent.get().getEventStatus());
  }

  @Test
  public void deleteEvent() {
    String reasonMessage = "Some reasons";
    String contact = "0923456326";
    Image image =
        new Image(
            new ObjectId("66c648b600179737a3d5c543"), "imageTestname", null, null, null, false);
    EventDeleteRequest eventDTO = new EventDeleteRequest();
    eventDTO.setId("66c648b600179737a3d5c123");
    eventDTO.setUserId(userOne.getId().toHexString());
    eventDTO.setEventId(event.getId().toHexString());
    eventDTO.setContact(contact);
    eventDTO.setReason(reasonMessage);
    event.setHasCancelRequest(true);
    event.setEventStatus(EventStatus.PENDING);
    event.setImages(List.of(image));
    imageRepository.save(image);
    eventRepository.save(event);
    eventCancelRepository.save(eventDTO);

    eventServiceImpl.deleteEvent(event.getId().toHexString(), eventDTO.getId());
    Optional<EventDeleteRequest> existingDeleteRequest =
        eventCancelRepository.findByEventId(event.getId().toHexString());
    Optional<Event> existingEvent = eventRepository.findById(event.getId());

    assertFalse(existingDeleteRequest.isPresent());
    assertFalse(existingEvent.isPresent());
  }

  @Test
  @DisplayName("Test EventServiceImpl method getEvents")
  void testMethodGetEvents() {
    String cityName = "Київ";
    Pageable pageable = PageRequest.of(0, 6);
    event.setEventStatus(EventStatus.APPROVED);
    eventRepository.save(event);

    Page<EventResponseDto> result = eventServiceImpl.getApprovedEvents(pageable, cityName);
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.getContent().size()),
        () -> assertEquals(event.getId().toHexString(), result.getContent().get(0).getId()),
        () -> assertNull(result.getContent().get(0).getOrganizers()));
  }

  @Nested
  @DisplayName("Test EventServiceImpl method getNewEvents.")
  class testGetNewEvents {
    @Test
    @DisplayName(
        "Test EventServiceImpl method getNewEvents without param city name. Positive Scenario return random list of events. Positive Scenario return random list of events.")
    public void testGetNewEventsPositiveScenarioWithoutCity() {
      Integer size = 2;
      String cityName = null;
      LocalDateTime date = LocalDateTime.now();
      event.setEventStatus(EventStatus.APPROVED);
      event.setCreationDate(date.minusDays(1));
      eventRepository.save(event);

      List<EventResponseDto> result = eventServiceImpl.getNewEvents(size, cityName);

      assertAll(
          () -> assertFalse(result.isEmpty()),
          () -> assertEquals(1, result.size()),
          () -> assertEquals(event.getId().toHexString(), result.get(0).getId()),
          () -> assertNull(result.get(0).getOrganizers()));
    }

    @Test
    @DisplayName(
        "Test EventServiceImpl method getNewEvents with param city name. Positive Scenario return random list of events.")
    public void testGetNewEventsPositiveScenarioWithCity() {
      Integer size = 2;
      String cityName = "Київ";
      LocalDateTime date = LocalDateTime.now();
      event.setEventStatus(EventStatus.APPROVED);
      event.setCreationDate(date.minusDays(1));

      eventRepository.save(event);

      List<EventResponseDto> result = eventServiceImpl.getNewEvents(size, cityName);

      assertAll(
          () -> assertFalse(result.isEmpty()),
          () -> assertEquals(1, result.size()),
          () -> assertEquals(event.getId().toHexString(), result.get(0).getId()),
          () -> assertNull(result.get(0).getOrganizers()),
          () -> assertEquals(cityName, result.get(0).getLocation().city()));
    }
  }

  public String helpMethodMakeUpdateEventRequest(EventUpdateDTO eventDTO) {
    MultipartFile firstImage = mock(MultipartFile.class);
    MultipartFile secondImage = mock(MultipartFile.class);
    MultipartFile thirdImage = mock(MultipartFile.class);

    String responseMessage =
        eventServiceImpl.createUpdateEventRequest(
            event.getId().toHexString(), eventDTO, userOne.getId().toHexString(), null, null);
    return responseMessage;
  }
}
