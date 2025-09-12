package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.entity.CityList;
import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.*;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.EventMapper;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.CloudinaryService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class EventServiceImplTest {

  @Mock private EventRepository eventRepository;

  @InjectMocks private EventServiceImpl eventService;

  @Mock private EventMapper eventMapper;
  @Mock private CloudinaryService mediaService;
  @Mock private UserService userService;
  @Mock private MailService mailService;
  @Mock private UserMapper userMapper;
  @Mock private UserRepository userRepository;
  @Mock private CityList cityList;

  private Event event;
  private DateDetails dateDetails;
  private EventResponseDto eventResponseDto;
  DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("uk"));
  DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

  @BeforeEach
  void setUp() throws IOException {
    LocalTime localTime = LocalTime.now();
    List<Image> imageList =
        Arrays.asList(Image.builder().id(new ObjectId("66c648b600179737a3d5c212")).build());
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
    event.setOrganizers(
        User.builder()
            .id(new ObjectId("66c648b600179737a3d5c211"))
            .email("test@email.com")
            .createdEvents(new ArrayList<>())
            .build());
    event.setDate(
        new DateDetails(
            LocalDate.of(LocalDate.now().plusYears(1).getYear(), 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
    event.setHasUpdateRequest(false);
    event.setHasCancelRequest(false);

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

  @Nested
  @DisplayName("Test EventServiceImpl method getApprovedEvents.")
  class GetApprovedEvents {

    @Test
    @DisplayName(
        "Test EventServiceImpl method getApprovedEvents with param city name. Positive Scenario return events.")
    public void testMethodGetApprovedEventsPositiveScenarioWithParamCityName() {
      String cityName = "Київ";
      Pageable pageable = PageRequest.of(0, 6);
      PageImpl<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);
      when(eventRepository.findEventByEventStatusAndLocation_City(
              EventStatus.APPROVED, cityName, pageable))
          .thenReturn(eventPage);
      when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);
      when(cityList.getCityList()).thenReturn(List.of("Київ"));
      Page<EventResponseDto> result = eventService.getApprovedEvents(pageable, cityName);
      assertAll(
          () -> assertFalse(result.isEmpty()),
          () -> assertEquals(1, result.getContent().size()),
          () -> assertEquals(event.getId().toHexString(), result.getContent().get(0).getId()),
          () -> assertNull(result.getContent().get(0).getOrganizers()));

      verify(eventRepository, times(1))
          .findEventByEventStatusAndLocation_City(EventStatus.APPROVED, cityName, pageable);
    }

    @Test
    @DisplayName(
            "Test EventServiceImpl method getApprovedEvents with param city name. Positive Scenario return events if city search results 0.")
    public void testMethodGetApprovedEventsPositiveScenarioReturnAllApprovedEvents() {
      String cityName = "Київ";
      Pageable pageable = PageRequest.of(0, 6);
      PageImpl<Event> eventPageNullContent = new PageImpl<>(List.of(), pageable, 1);
      PageImpl<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);
      when(eventRepository.findEventByEventStatusAndLocation_City(
              EventStatus.APPROVED, cityName, pageable))
              .thenReturn(eventPageNullContent);
      when(eventRepository.findEventByEventStatus(EventStatus.APPROVED, pageable))
              .thenReturn(eventPage);
      when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);
      when(cityList.getCityList()).thenReturn(List.of("Київ"));
      Page<EventResponseDto> result = eventService.getApprovedEvents(pageable, cityName);
      assertAll(
              () -> assertFalse(result.isEmpty()),
              () -> assertEquals(1, result.getContent().size()),
              () -> assertEquals(event.getId().toHexString(), result.getContent().get(0).getId()),
              () -> assertNull(result.getContent().get(0).getOrganizers()));

      verify(eventRepository, times(1))
              .findEventByEventStatusAndLocation_City(EventStatus.APPROVED, cityName, pageable);
      verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.APPROVED, pageable);
    }

    @Test
    @DisplayName(
        "Test EventServiceImpl method getApprovedEvents without param city name. Positive Scenario return events.")
    public void testMethodApprovedGetEventsPositiveScenarioWithoutParamCityName() {
      String cityName = null;
      Pageable pageable = PageRequest.of(0, 6);
      PageImpl<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);
      when(eventRepository.findEventByEventStatus(EventStatus.APPROVED, pageable))
          .thenReturn(eventPage);
      when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);
      Page<EventResponseDto> result = eventService.getApprovedEvents(pageable, cityName);
      assertAll(
          () -> assertFalse(result.isEmpty()),
          () -> assertEquals(1, result.getContent().size()),
          () -> assertEquals(event.getId().toHexString(), result.getContent().get(0).getId()),
          () -> assertNull(result.getContent().get(0).getOrganizers()));

      verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.APPROVED, pageable);
    }

    @Test
    @DisplayName(
        "Test EventServiceImpl method getApprovedEvents. Negative Scenario throw exception when cityName was entered incorrectly.")
    public void testMethodGetApprovedEventsNegativeScenarioThrowException() {
      String cityName = "null";
      String errorMessage = "The city name was entered incorrectly.";
      Pageable pageable = PageRequest.of(0, 6);
      //
      // when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);
      GeneralException result =
          assertThrows(
              GeneralException.class, () -> eventService.getApprovedEvents(pageable, cityName));

      assertEquals(HttpStatus.BAD_REQUEST, result.getHttpStatus());
      assertEquals(errorMessage, result.getMessage());

      verify(eventRepository, times(0)).findEventByEventStatus(EventStatus.APPROVED, pageable);
      verify(eventRepository, times(0))
          .findEventByEventStatusAndLocation_City(EventStatus.APPROVED, cityName, pageable);
    }
  }

  //  @Test
  //  void testUpdateEventStatus() {
  //    event.setEventStatus(EventStatus.APPROVED);
  //    eventResponseDto.setEventStatus(event.getEventStatus().toString());
  //    when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
  //    when(eventRepository.save(event)).thenReturn(event);
  //
  // when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);
  //
  //    EventResponseDto result = eventService.updateEventStatus(event.getId().toHexString(),
  // EventStatus.APPROVED.toString());
  //
  //    assertEquals(EventStatus.APPROVED.toString(), result.getEventStatus());
  //    verify(eventRepository, times(1)).findById(event.getId());
  //    verify(eventRepository, times(1)).save(event);
  //    verify(eventMapper, times(1)).toEventResponseDtoFromEventWithoutUser(event);
  //
  //  }

  @Test
  @DisplayName("Test EventServiceImpl method GetEventsByStatus find PENDING events")
  void testMethodGetEventsByStatusPENDING() {
    Pageable pageable = PageRequest.of(0, 6);
    event.setEventStatus(EventStatus.PENDING);
    PageImpl<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);
    when(eventRepository.findEventByEventStatus(EventStatus.PENDING, pageable))
        .thenReturn(eventPage);
    when(eventMapper.toEventResponseDtoFromEvent(event, eventResponseDto.getOrganizers()))
        .thenReturn(eventResponseDto);
    Page<EventResponseDto> result =
        eventService.getEventsByStatus(EventStatus.PENDING.toString(), pageable);
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.getContent().size()),
        () -> assertEquals(event.getId().toHexString(), result.getContent().get(0).getId()),
        () ->
            assertEquals(
                EventStatus.PENDING.toString(), result.getContent().get(0).getEventStatus()));

    verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.PENDING, pageable);
  }

  @Test
  @DisplayName("Test EventServiceImpl method GetEventsByStatus find APPROVED events")
  void testMethodGetEventsByStatusAPPROVED() {
    Pageable pageable = PageRequest.of(0, 6);
    event.setEventStatus(EventStatus.APPROVED);
    PageImpl<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);
    eventResponseDto.setEventStatus(event.getEventStatus().toString());
    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED, pageable))
        .thenReturn(eventPage);
    when(eventMapper.toEventResponseDtoFromEvent(event, eventResponseDto.getOrganizers()))
        .thenReturn(eventResponseDto);
    Page<EventResponseDto> result =
        eventService.getEventsByStatus(EventStatus.APPROVED.toString(), pageable);
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.getContent().size()),
        () -> assertEquals(event.getId().toHexString(), result.getContent().get(0).getId()),
        () ->
            assertEquals(
                EventStatus.APPROVED.toString(), result.getContent().get(0).getEventStatus()));

    verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.APPROVED, pageable);
  }

  @Test
  @DisplayName("Test EventServiceImpl method countByStatus.")
  void countByStatus() {

    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED)).thenReturn(List.of(event));
    when(eventRepository.findEventByEventStatus(EventStatus.CANCELLED)).thenReturn(List.of());
    when(eventRepository.findEventByEventStatus(EventStatus.PENDING)).thenReturn(List.of(event));

    Map<String, Integer> countByStatus = eventService.countByStatus();
    assertAll(
        () -> assertFalse(countByStatus.isEmpty()),
        () -> assertEquals(1, countByStatus.get(EventStatus.APPROVED.toString())),
        () -> assertEquals(1, countByStatus.get(EventStatus.PENDING.toString())),
        () -> assertEquals(0, countByStatus.get(EventStatus.CANCELLED.toString())));
  }

  @Test
  @DisplayName(
      "Test EventServiceImpl method approveEvent. Positive Scenario change event status to approved.")
  void testMethodApproveEventPositiveScenarioWithParamCityName() {
    event.setEventStatus(EventStatus.APPROVED);
    eventResponseDto.setEventStatus(event.getEventStatus().toString());
    when(eventRepository.findById(event.getId())).thenReturn(Optional.ofNullable(event));
    when(eventRepository.save(event)).thenReturn(event);
    when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);

    EventResponseDto result = eventService.approveEvent(event.getId().toHexString());
    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(event.getId().toHexString(), result.getId()),
        () -> assertEquals(EventStatus.APPROVED.toString(), result.getEventStatus()));
  }

  @Test
  public void testCreateEvent() {

    EventDTO eventDTO = new EventDTO();
    eventDTO.setOrganizers(
        UserResponseDto.builder()
            .id(event.getOrganizers().getId().toHexString())
            .email(event.getOrganizers().getEmail())
            .build());
    eventDTO.setTitle("anyString");
    eventDTO.setLocation(
        new Location("Київ", "вул. Академіка Баха, 3", "", "50.417223", "30.524763"));
    MultipartFile firstImage = mock(MultipartFile.class);
    MultipartFile secondImage = mock(MultipartFile.class);
    MultipartFile thirdImage = mock(MultipartFile.class);

    UserResponseDto userResponseDto = new UserResponseDto();
    userResponseDto.setId(eventDTO.getOrganizers().getId());
    userResponseDto.setEmail(eventDTO.getOrganizers().getEmail());
    eventResponseDto.setOrganizers(userResponseDto);

    when(userService.findUserById(eventDTO.getOrganizers().getId()))
        .thenReturn(event.getOrganizers());
    List<MultipartFile> multipartFiles = Arrays.asList(firstImage, secondImage, thirdImage);
    when(mediaService.savedEventImages(multipartFiles, "anyString"))
        .thenReturn(Collections.emptyList());
    when(eventMapper.toEvent(eventDTO)).thenReturn(event);
    when(eventRepository.save(event)).thenReturn(event);
    doNothing()
        .when(mailService)
        .sendSimpleHtmlMailMessage4Line(
            eventDTO.getOrganizers().getEmail(),
            "Твоя подія успішно створена на BookMyEvent",
            "Вітаємо! Твоя подія успішно створена \uD83C\uDF89",
            "Вона зараз на перевірці адміністратором. Очікуй оновлення статусу протягом 48 годин або переглядай його в розділі <strong>\"Мої події\"</strong>.",
            "Після підтвердження подія з’явиться на сайті та стане доступною для покупки всім користувачам.",
            "Дякуємо, що обрали нашу платформу! Якщо у тебе є запитання, звертайся до нашої служби підтримки.",
            "");
    when(eventMapper.toEventResponseDtoFromEvent(event, userResponseDto))
        .thenReturn(eventResponseDto);
    when(userMapper.toUserResponseDtoWithoutAvatarAndEvents(event.getOrganizers()))
        .thenReturn(userResponseDto);
    when(userRepository.save(event.getOrganizers())).thenReturn(event.getOrganizers());

    EventResponseDto result =
        eventService.createEvent(eventDTO, firstImage, secondImage, thirdImage);

    assertNotNull(result);
    assertEquals(EventStatus.PENDING.toString(), result.getEventStatus());
    assertEquals(event.getId().toHexString(), result.getId());
    assertEquals(event.getOrganizers().getId().toHexString(), result.getOrganizers().getId());
    assertEquals(event.getImages().get(0).getId(), result.getImages().get(0).getId());

    verify(eventRepository, times(1)).save(any(Event.class));
  }

  @Test
  public void updateEventTest() {}

  @Test
  @DisplayName(
      "Test EventServiceImpl method getTopEvents. Positive Scenario return random list of events.")
  public void testGetTopEventsPositiveScenario() {}

  @Nested
  @DisplayName(
          "Test EventServiceImpl method getNewEvents.")
  class testGetNewEvents {

    @Test
    @DisplayName(
        "Test EventServiceImpl method getNewEvents without param city name. Positive Scenario return random list of events.")
    public void testGetNewEventsPositiveScenarioWithoutCity() {
      Integer size = 2;
      String cityName = null;
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      LocalDateTime fromDate = LocalDateTime.now();
      LocalDateTime toDate = fromDate.minusDays(7);
      event.setEventStatus(EventStatus.APPROVED);

      when(eventRepository.findRandomEventsByCreationDate(
              any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED), eq(size)))
          .thenReturn(List.of(event));
      when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);

      List<EventResponseDto> result = eventService.getNewEvents(size, cityName);

      assertAll(
          () -> assertFalse(result.isEmpty()),
          () -> assertEquals(1, result.size()),
          () -> assertEquals(event.getId().toHexString(), result.get(0).getId()),
          () -> assertNull(result.get(0).getOrganizers()));

      verify(eventRepository, times(1))
          .findRandomEventsByCreationDate(
                  any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED), eq(size));
    }

    @Test
    @DisplayName(
        "Test EventServiceImpl method getNewEvents with param city name. Positive Scenario return random list of events.")
    public void testGetNewEventsPositiveScenarioWithCity() {
      Integer size = 2;
      String cityName = "Київ";
      LocalDateTime fromDate = LocalDateTime.now();
      LocalDateTime toDate = fromDate.minusDays(7);
      event.setEventStatus(EventStatus.APPROVED);

      when(eventRepository.findRandomEventsByCreationDateByCity(
              any(LocalDateTime.class), any(LocalDateTime.class),eq(cityName), eq(EventStatus.APPROVED), eq(size)))
          .thenReturn(List.of(event));
      when(cityList.getCityList()).thenReturn(List.of("Київ"));
      when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);

      List<EventResponseDto> result = eventService.getNewEvents(size, cityName);

      assertAll(
          () -> assertFalse(result.isEmpty()),
          () -> assertEquals(1, result.size()),
          () -> assertEquals(event.getId().toHexString(), result.get(0).getId()),
          () -> assertNull(result.get(0).getOrganizers()));

      verify(eventRepository, times(1))
          .findRandomEventsByCreationDateByCity(
                  any(LocalDateTime.class), any(LocalDateTime.class),eq(cityName), eq(EventStatus.APPROVED), eq(size));
    }

    @Test
    @DisplayName(
            "Test EventServiceImpl method getNewEvents with param city name. Positive Scenario return random list of all events if city search results 0.")
    public void testGetNewEventsPositiveScenarioReturnAllApprovedEvents() {
      Integer size = 2;
      String cityName = "Київ";
      LocalDateTime fromDate = LocalDateTime.now();
      LocalDateTime toDate = fromDate.minusDays(7);
      event.setEventStatus(EventStatus.APPROVED);

      when(eventRepository.findRandomEventsByCreationDateByCity(
              any(LocalDateTime.class), any(LocalDateTime.class),eq(cityName), eq(EventStatus.APPROVED), eq(size)))
              .thenReturn(List.of());
      when(eventRepository.findRandomEventsByCreationDate(
              any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED), eq(size)))
              .thenReturn(List.of(event));
      when(cityList.getCityList()).thenReturn(List.of("Київ"));
      when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);

      List<EventResponseDto> result = eventService.getNewEvents(size, cityName);

      assertAll(
              () -> assertFalse(result.isEmpty()),
              () -> assertEquals(1, result.size()),
              () -> assertEquals(event.getId().toHexString(), result.get(0).getId()),
              () -> assertNull(result.get(0).getOrganizers()));

      verify(eventRepository, times(1))
              .findRandomEventsByCreationDate(
                      any(LocalDateTime.class), any(LocalDateTime.class), eq(EventStatus.APPROVED), eq(size));
      verify(eventRepository, times(1))
              .findRandomEventsByCreationDateByCity(
                      any(LocalDateTime.class), any(LocalDateTime.class),eq(cityName), eq(EventStatus.APPROVED), eq(size));
    }
  }
}
