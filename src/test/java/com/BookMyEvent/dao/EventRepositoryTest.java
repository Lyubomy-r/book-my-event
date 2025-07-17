package com.BookMyEvent.dao;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.entity.CityList;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.dto.EventFilterRequest;
import com.BookMyEvent.mapper.EventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Testcontainers
@Import({TestConfig.class})
@Slf4j
class EventRepositoryTest {

  @Container
  private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

  @DynamicPropertySource
  static void mongoDbProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired private EventMapper eventMapper;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private EventRepository eventRepository;
  @Autowired private Clock clock;
  @Autowired private MongoTemplate mongoTemplate;
  @Autowired private CityList cityList;

  private Event event;
  private Event event2;
  private Event event3;

  @BeforeEach
  void setUp() throws IOException {
    eventRepository.deleteAll();
    LocalTime localTime = LocalTime.now();
    LocalDate now = LocalDate.now();
    event = new Event();
    event.setId(new ObjectId("66c648b600179737a3d5c235"));
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS);
    event.setEventCategory(EventCategory.TOP_EVENTS);
    event.setEventStatus(EventStatus.APPROVED);
    event.setLocation(new Location("Київ", "вул. Успішна, 1", "", "50.426129", "30.514067"));
    event.setAvailableTickets(100);
    event.setNumberOfTickets(100);
    event.setDate(
        new DateDetails(
            LocalDate.of(2025, 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
    event2 = new Event();
    event2.setId(new ObjectId("66c648b600179737a3d5c233"));
    event2.setTitle("Test Event22");
    event2.setDescription("Test Description");
    event2.setEventType(EventType.SPORTS_EVENTS);
    event2.setEventCategory(EventCategory.TOP_EVENTS);
    event2.setEventStatus(EventStatus.APPROVED);
    event2.setAvailableTickets(100);
    event2.setNumberOfTickets(100);
    event2.setDate(
        new DateDetails(
            LocalDate.now().toString(), localTime.toString(), localTime.plusHours(2L).toString()));
    event2.setLocation(new Location("Вінниця", "вул. Успішна, 1", "", "50.426129", "30.514067"));
    event3 = new Event();
    event3.setId(new ObjectId("66c648b600179737a3d5c243"));
    event3.setTitle("Test Event33");
    event3.setDescription("Test Description");
    event3.setEventType(EventType.SPORTS_EVENTS);
    event3.setEventCategory(EventCategory.TOP_EVENTS);
    event3.setEventStatus(EventStatus.APPROVED);
    event3.setAvailableTickets(100);
    event3.setNumberOfTickets(100);
    event3.setDate(
        new DateDetails(now.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
    //    event.setDate(
    //        new DateDetails(
    //            LocalDate.now().toString(), localTime.toString(),
    // localTime.plusHours(2L).toString()));
    cityList.init();
  }

  @Test
  @DisplayName("Test EventRepository method FindEventNumberOfTickets PositiveScenario")
  public void testMethodFindEventNumberOfTicketsPositiveScenario() {
    eventRepository.save(event);
    Optional<Event> result = eventRepository.findEventNumberOfTickets(event.getId().toHexString());
    assertTrue(result.isPresent());
    assertEquals(100, result.get().getNumberOfTickets());
  }

  @Test
  @DisplayName("Test EventRepository method FindByEventStartDate PositiveScenario")
  public void testMethodFindByEventStartDatePositiveScenario() {

    eventRepository.save(event);
    LocalDate now = LocalDate.now();
    List<Event> results = eventRepository.findByEventStartDate(event.getDate().day());
    System.out.println("results " + results);
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getId()).isEqualTo(event.getId());
  }

  @Test
  @DisplayName("Test EventRepository method FindAll PositiveScenario")
  public void testMethodFindAllPositiveScenario() {

    eventRepository.save(event);

    List<Event> results = eventRepository.findAll();

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getId()).isEqualTo(event.getId());
  }

  @RepeatedTest(4)
  @DisplayName(
      "Test EventRepository method findRandomEventsByCategory. Positive Scenario return random list of events.")
  public void testFindRandomEventsByCategoryPositiveScenario() {
    LocalTime localTime = LocalTime.now();
    Event event2 = new Event();
    event2.setId(new ObjectId("66c648b600179737a3d5c233"));
    event2.setTitle("Test Event22");
    event2.setDescription("Test Description");
    event2.setEventType(EventType.SPORTS_EVENTS);
    event2.setEventCategory(EventCategory.TOP_EVENTS);
    event2.setEventStatus(EventStatus.APPROVED);
    event2.setAvailableTickets(100);
    event2.setNumberOfTickets(100);
    event2.setDate(
        new DateDetails(
            LocalDate.of(2025, 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
    Event event3 = new Event();
    event3.setId(new ObjectId("66c648b600179737a3d5c234"));
    event3.setTitle("Test Event33");
    event3.setDescription("Test Description");
    event3.setEventType(EventType.SPORTS_EVENTS);
    event3.setEventCategory(EventCategory.TOP_EVENTS);
    event3.setAvailableTickets(100);
    event3.setNumberOfTickets(100);
    event3.setDate(
        new DateDetails(
            LocalDate.of(2025, 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
    event3.setEventStatus(EventStatus.APPROVED);
    Event event4 = new Event();
    event4.setId(new ObjectId("66c648b600179737a3d5c238"));
    event4.setTitle("Test Event44");
    event4.setDescription("Test Description");
    event4.setEventType(EventType.SPORTS_EVENTS);
    event4.setEventCategory(EventCategory.TOP_EVENTS);
    event4.setEventStatus(EventStatus.APPROVED);
    event4.setAvailableTickets(100);
    event4.setNumberOfTickets(100);
    event4.setDate(
        new DateDetails(
            LocalDate.of(2025, 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
    Event event5 = new Event();
    event5.setId(new ObjectId("66c648b600179737a3d5c245"));
    event5.setTitle("Test Event55");
    event5.setDescription("Test Description");
    event5.setEventType(EventType.SPORTS_EVENTS);
    event5.setEventCategory(EventCategory.TOP_EVENTS);
    event5.setEventStatus(EventStatus.APPROVED);
    event5.setAvailableTickets(100);
    event5.setNumberOfTickets(100);
    event5.setDate(
        new DateDetails(
            LocalDate.of(2025, 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
    eventRepository.saveAll(List.of(event, event2, event4, event3, event5));

    List<Event> eventList =
        eventRepository.findRandomEventsByCategory(
            EventCategory.TOP_EVENTS, EventStatus.APPROVED, 2);
    List<Event> eventList2 =
        eventRepository.findRandomEventsByCategory(
            EventCategory.TOP_EVENTS, EventStatus.APPROVED, 2);

    assertEquals(2, eventList.size());
    assertEquals(2, eventList.size());
    assertFalse(eventList.containsAll(eventList2));
  }

  @Test
  @DisplayName("Test EventRepository method filterEvents. Positive Scenario return page of events.")
  public void testFilterEventsPositiveScenario() {
    LocalTime localTime = LocalTime.now();
    LocalDate sunday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    log.info("LocalDate - sunday {}", sunday);
    Event event2 = new Event();
    event2.setId(new ObjectId("66c648b600179737a3d5c233"));
    event2.setTitle("Test Event22");
    event2.setDescription("Test Description");
    event2.setEventType(EventType.SPORTS_EVENTS);
    event2.setEventCategory(EventCategory.TOP_EVENTS);
    event2.setEventStatus(EventStatus.APPROVED);
    event2.setAvailableTickets(100);
    event2.setNumberOfTickets(100);
    event2.setDate(
        new DateDetails(
            sunday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
    eventRepository.saveAll(List.of(event, event2));
    EventRepositoryCustomImpl eventService =
        Mockito.spy(new EventRepositoryCustomImpl(mongoTemplate, Clock.systemUTC(), cityList));
    Mockito.doReturn(sunday).when(eventService).getCurrentDate();
    EventFilterRequest eventFilterRequest =
        new EventFilterRequest(
            List.of(EventType.SPORTS_EVENTS),
            false,
            false,
            false,
            true,
            false,
            null,
            false,
            false,
            null,
            null,
            null,
            null);

    Page<Event> eventPage = eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

    assertEquals(1, eventPage.getContent().size());
    assertTrue(eventPage.getContent().contains(event2));
    assertEquals(sunday.toString(), eventPage.getContent().get(0).getDate().day());
  }

  @Nested
  @DisplayName("Test EventRepository method FilterEvents (IsOnTheWeekend).")
  class testFilterEventsByIsOnTheWeekend {
    @Test
    @DisplayName(
        "Test EventRepository method filterEvents. Positive Scenario return page of events Find By Is On TheWeekend. Start saturday")
    public void testFilterEventsPositiveScenarioFindByIsOnTheWeekendStartSaturday() {
      LocalTime localTime = LocalTime.now();
      LocalDate fixedDate = LocalDate.of(2025, 7, 3);
      LocalDate saturday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      LocalDate sunday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      log.info("LocalDate - now {}", fixedDate);
      log.info("LocalDate - saturday {}", saturday);
      log.info("LocalDate - sunday {}", sunday);
      event2.setDate(
          new DateDetails(
              sunday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event3.setDate(
          new DateDetails(
              fixedDate.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event.setDate(
          new DateDetails(
              saturday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      eventRepository.saveAll(List.of(event, event2, event3));
      Clock fixedClock =
          Clock.fixed(saturday.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
      log.info("LocalDate - fixedClock saturday {}", fixedClock);
      EventRepositoryCustomImpl eventRepository =
          new EventRepositoryCustomImpl(mongoTemplate, fixedClock, cityList);
      EventFilterRequest eventFilterRequest =
          new EventFilterRequest(
              List.of(EventType.SPORTS_EVENTS),
              false,
              false,
              false,
              true,
              false,
              null,
              false,
              false,
              null,
              null,
              null,
              null);

      Page<Event> eventPage =
          eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

      assertEquals(2, eventPage.getContent().size());
      assertTrue(eventPage.getContent().containsAll(List.of(event, event2)));
      assertFalse(eventPage.getContent().contains(event3));
    }

    @Test
    @DisplayName(
        "Test EventRepository method filterEvents. Positive Scenario return page of events Find By Is On TheWeekend. Start now")
    public void testFilterEventsPositiveScenarioFindByIsOnTheWeekendStartNow() {
      LocalTime localTime = LocalTime.now();
      LocalDate fixedDate = LocalDate.of(2025, 7, 3);
      LocalDate saturday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      LocalDate sunday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      log.info("LocalDate - now {}", fixedDate);
      log.info("LocalDate - saturday {}", saturday);
      log.info("LocalDate - sunday {}", sunday);
      event2.setDate(
          new DateDetails(
              sunday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event3.setDate(
          new DateDetails(
              fixedDate.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event.setDate(
          new DateDetails(
              saturday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      eventRepository.saveAll(List.of(event, event2, event3));
      Clock fixedClock =
          Clock.fixed(fixedDate.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
      log.info("LocalDate - fixedClock saturday {}", fixedClock);
      EventRepositoryCustomImpl eventRepository =
          new EventRepositoryCustomImpl(mongoTemplate, fixedClock, cityList);
      EventFilterRequest eventFilterRequest =
          new EventFilterRequest(
              null, false, false, false, true, false, null, false, false, null, null, null, null);

      Page<Event> eventPage =
          eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

      assertEquals(2, eventPage.getContent().size());
      assertTrue(eventPage.getContent().containsAll(List.of(event, event2)));
      assertFalse(eventPage.getContent().contains(event3));
    }

    @Test
    @DisplayName(
        "Test EventRepository method filterEvents. Positive Scenario return page of events Find By Is On TheWeekend. Start sunday")
    public void testFilterEventsPositiveScenarioFindByIsOnTheWeekendStartSunday() {
      LocalTime localTime = LocalTime.now();
      LocalDate fixedDate = LocalDate.of(2025, 7, 3);
      LocalDate saturday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      LocalDate sunday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      log.info("LocalDate - now {}", fixedDate);
      log.info("LocalDate - saturday {}", saturday);
      log.info("LocalDate - sunday {}", sunday);
      event2.setDate(
          new DateDetails(
              sunday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event3.setDate(
          new DateDetails(
              fixedDate.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event.setDate(
          new DateDetails(
              saturday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      eventRepository.saveAll(List.of(event, event2, event3));
      Clock fixedClock =
          Clock.fixed(sunday.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
      log.info("LocalDate - fixedClock saturday {}", fixedClock);
      EventRepositoryCustomImpl eventRepository =
          new EventRepositoryCustomImpl(mongoTemplate, fixedClock, cityList);
      EventFilterRequest eventFilterRequest =
          new EventFilterRequest(
              List.of(EventType.SPORTS_EVENTS),
              false,
              false,
              false,
              true,
              false,
              null,
              false,
              false,
              null,
              null,
              null,
              null);

      Page<Event> eventPage =
          eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

      assertEquals(1, eventPage.getContent().size());
      assertTrue(eventPage.getContent().contains(event2));
      assertFalse(eventPage.getContent().containsAll(List.of(event, event3)));
    }
  }

  @Nested
  @DisplayName("Test EventRepository method FilterEvents (IsThisWeek).")
  class testFilterEventsByIsThisWeek {
    @Test
    @DisplayName(
        "Test EventRepository method filterEvents. Positive Scenario return page of events Find By is This Week. Start Saturday")
    public void testFilterEventsPositiveScenarioFindByIsThisWeekStartSaturday() {
      LocalTime localTime = LocalTime.now();
      LocalDate fixedDate = LocalDate.of(2025, 7, 3);
      LocalDate saturday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      LocalDate sunday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      log.info("LocalDate - now {}", fixedDate);
      log.info("LocalDate - saturday {}", saturday);
      log.info("LocalDate - sunday {}", sunday);
      event2.setDate(
          new DateDetails(
              sunday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event3.setDate(
          new DateDetails(
              fixedDate.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event.setDate(
          new DateDetails(
              saturday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      eventRepository.saveAll(List.of(event, event2, event3));
      Clock fixedClock =
          Clock.fixed(saturday.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
      log.info("LocalDate - fixedClock saturday {}", fixedClock);
      EventRepositoryCustomImpl eventRepository =
          new EventRepositoryCustomImpl(mongoTemplate, fixedClock, cityList);
      EventFilterRequest eventFilterRequest =
          new EventFilterRequest(
              List.of(EventType.SPORTS_EVENTS),
              false,
              false,
              false,
              false,
              true,
              null,
              false,
              false,
              null,
              null,
              null,
              null);

      Page<Event> eventPage =
          eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

      assertEquals(2, eventPage.getContent().size());
      assertTrue(eventPage.getContent().containsAll(List.of(event, event2)));
      assertFalse(eventPage.getContent().contains(event3));
    }

    @Test
    @DisplayName(
        "Test EventRepository method filterEvents. Positive Scenario return page of events Find By is This Week. Start Now")
    public void testFilterEventsPositiveScenarioFindByIsThisWeekStartNow() {
      LocalTime localTime = LocalTime.now();
      LocalDate fixedDate = LocalDate.of(2025, 7, 3);
      LocalDate saturday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      LocalDate sunday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      log.info("LocalDate - now {}", fixedDate);
      log.info("LocalDate - saturday {}", saturday);
      log.info("LocalDate - sunday {}", sunday);
      event2.setDate(
          new DateDetails(
              sunday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event3.setDate(
          new DateDetails(
              fixedDate.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event.setDate(
          new DateDetails(
              saturday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      eventRepository.saveAll(List.of(event, event2, event3));
      Clock fixedClock =
          Clock.fixed(fixedDate.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
      log.info("LocalDate - fixedClock now {}", fixedClock);
      EventRepositoryCustomImpl eventRepository =
          new EventRepositoryCustomImpl(mongoTemplate, fixedClock, cityList);
      EventFilterRequest eventFilterRequest =
          new EventFilterRequest(
              List.of(EventType.SPORTS_EVENTS),
              false,
              false,
              false,
              false,
              true,
              null,
              false,
              false,
              null,
              null,
              null,
              null);

      Page<Event> eventPage =
          eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

      assertEquals(3, eventPage.getContent().size());
      assertTrue(eventPage.getContent().containsAll(List.of(event, event2, event3)));
      //    assertFalse(eventPage.getContent().contains(event3));
    }

    @Test
    @DisplayName(
        "Test EventRepository method filterEvents. Positive Scenario return page of events Find By is This Week. Start sunday")
    public void testFilterEventsPositiveScenarioFindByIsThisWeekStartSunday() {
      LocalTime localTime = LocalTime.now();
      LocalDate fixedDate = LocalDate.of(2025, 7, 3);
      LocalDate saturday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      LocalDate sunday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      log.info("LocalDate - now {}", fixedDate);
      log.info("LocalDate - saturday {}", saturday);
      log.info("LocalDate - sunday {}", sunday);
      event2.setDate(
          new DateDetails(
              sunday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event.setDate(
          new DateDetails(
              saturday.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      event3.setDate(
          new DateDetails(
              fixedDate.toString(), localTime.toString(), localTime.plusHours(2L).toString()));
      eventRepository.saveAll(List.of(event, event2, event3));
      Clock fixedClock =
          Clock.fixed(sunday.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
      log.info("LocalDate - fixedClock now {}", fixedClock);
      EventRepositoryCustomImpl eventRepository =
          new EventRepositoryCustomImpl(mongoTemplate, fixedClock, cityList);
      EventFilterRequest eventFilterRequest =
          new EventFilterRequest(
              List.of(EventType.SPORTS_EVENTS),
              false,
              false,
              false,
              false,
              true,
              null,
              false,
              false,
              null,
              null,
              null,
              null);

      Page<Event> eventPage =
          eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

      assertEquals(1, eventPage.getContent().size());
      assertTrue(eventPage.getContent().contains(event2));
      assertFalse(eventPage.getContent().containsAll(List.of(event3, event)));
    }
  }

  @Nested
  @DisplayName("Test EventRepository method FilterEvents (CiteName).")
  class testFilterEventsByCiteName {
    @Test
    @DisplayName(
        "Test EventRepository method filterEvents. Positive Scenario return page of events Find By is This Week. Start Saturday")
    public void testFilterEventsPositiveScenarioFindByIsThisWeekStartSaturday() {
      LocalDate fixedDate = LocalDate.of(2025, 7, 3);
      LocalDate saturday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      eventRepository.saveAll(List.of(event, event2));
      Clock fixedClock =
          Clock.fixed(saturday.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
      EventRepositoryCustomImpl eventRepository =
          new EventRepositoryCustomImpl(mongoTemplate, fixedClock, cityList);
      EventFilterRequest eventFilterRequest =
          new EventFilterRequest(
             null,
              false,
              false,
              false,
              false,
              false,
              null,
              false,
              false,
              null,
              null,
              null,
              "Київ");

      Page<Event> eventPage =
          eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

      assertEquals(1, eventPage.getContent().size());
      assertTrue(eventPage.getContent().contains(event));
      assertFalse(eventPage.getContent().contains(event2));
    }

    @Test
    @DisplayName(
            "Test EventRepository method filterEvents. Positive Scenario return page of events Find By is This Week. Start Saturdayaaa")
    public void testFilterEventsPositiveScenarioFindByIsThisWeekStartSaturdaya() {
      LocalDate fixedDate = LocalDate.of(2025, 7, 3);
      LocalDate saturday = fixedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      eventRepository.saveAll(List.of(event, event2));
      Clock fixedClock =
              Clock.fixed(saturday.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
      EventRepositoryCustomImpl eventRepository =
              new EventRepositoryCustomImpl(mongoTemplate, fixedClock, cityList);
      EventFilterRequest eventFilterRequest =
              new EventFilterRequest(
                      null,
                      false,
                      false,
                      false,
                      false,
                      false,
                      null,
                      false,
                      false,
                      null,
                      null,
                      null,
                      "Київd");

      Page<Event> eventPage =
              eventRepository.filterEvents(eventFilterRequest, PageRequest.of(0, 6));

      assertEquals(2, eventPage.getContent().size());
      assertTrue(eventPage.getContent().containsAll(List.of(event, event2)));
    }
  }
}
