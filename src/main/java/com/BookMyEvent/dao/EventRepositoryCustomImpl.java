package com.BookMyEvent.dao;

import com.BookMyEvent.entity.CityList;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.DateRange;
import com.BookMyEvent.entity.dto.EventFilterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

  private final MongoTemplate mongoTemplate;
  private final Clock clock;
  private final CityList cityList;

  private final String className = this.getClass().getSimpleName();

  @Override
  public Page<Event> filterEvents(EventFilterRequest filter, Pageable pageable) {
    List<Criteria> criteriaList = new ArrayList<>();
    List<Criteria> priceCriteriaList = new ArrayList<>();
    List<Criteria> dateCriteriaList = new ArrayList<>();

    // Event Types filter
    if (!CollectionUtils.isEmpty(filter.eventTypes())) {
      criteriaList.add(Criteria.where("eventType").in(filter.eventTypes()));
      log.info("Class: {}, Method: filterEvents - used filter eventTypes.", className);
    }

    criteriaList.add(Criteria.where("eventStatus").in(EventStatus.APPROVED));
    log.info("Class: {}, Method: filterEvents - used filter EventStatus APPROVED.", className);

    if (filter.cityName() != null && cityList.getCityList().contains(filter.cityName())) {
      criteriaList.add(Criteria.where("location.city").in(filter.cityName()));
      log.info("Class: {}, Method: filterEvents - used filter cityName {}.", className, filter.cityName());
    }

    // Location filter (під домом)
    if (Boolean.TRUE.equals(filter.isNearby()) && filter.latitude() != null && filter.longitude() != null) {
      GeoJsonPoint userLocation = new GeoJsonPoint(
          Double.parseDouble(filter.longitude()),
          Double.parseDouble(filter.latitude())
      );
      log.info("Longitude: {}, Latitude: {}", filter.longitude(), filter.latitude());
      log.info("Class: {}, Method: filterEvents - used filter try add Criteria isNearby.", className);
      criteriaList.add(Criteria.where("coordinates")
          .nearSphere(userLocation)
          .maxDistance(5000));
      log.info("Class: {}, Method: filterEvents - used filter isNearby: 5 km.", className);
    }

    // Popular events filter
    if (Boolean.TRUE.equals(filter.isPopular())) {
      criteriaList.add(Criteria.where("eventCategory").in(EventCategory.TOP_EVENTS));
      log.info("Class: {}, Method: filterEvents - used  filter isPopular: TOP_EVENTS.", className);
    }

    // Date filters
    addDateCriteria(filter, dateCriteriaList);

    // Price filters
    addPriceCriteria(filter, priceCriteriaList);

    Criteria finalCriteria = new Criteria();
    if (!criteriaList.isEmpty()) {
      finalCriteria.andOperator(criteriaList.toArray(new Criteria[0]));
      if (!priceCriteriaList.isEmpty()) {
        finalCriteria.orOperator(priceCriteriaList.toArray(new Criteria[0]));
      }
      if (!dateCriteriaList.isEmpty()) {
        finalCriteria.orOperator(dateCriteriaList.toArray(new Criteria[0]));
      }
    }
    Query queryCount = Query.query(finalCriteria);
    Query query = Query.query(finalCriteria).with(pageable);

//    log.info("Query count: {}", queryCount);
//    log.info("Query find: {}", query);

    long total = mongoTemplate.count(queryCount, Event.class);
    log.info("Class: {}, Method: filterEvents - count total APPROVED events {}", className, total);

    if (total == 0) {
      return Page.empty(pageable);
    }
    List<Event> events = mongoTemplate.find(query, Event.class);
    log.info("Class: {}, Method: filterEvents - Fetching all APPROVED events. size : {}", className, events.size());

    int pageSize = pageable.getPageSize();
    final int calculatedTotalPages = (int) Math.ceil((double) total / pageSize);

    final int totalPages = (total % pageSize == 0) ? calculatedTotalPages - 1 : calculatedTotalPages;
    log.info("Class: {}, Method: filterEvents - count totalPages APPROVED events {}", className, totalPages);

    return new PageImpl<>(events, pageable, total);
  }

  private void addDateCriteria(EventFilterRequest filter, List<Criteria> criteriaList) {
    LocalDate now = getCurrentDate();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    log.info("Class: {}, Method: addDateCriteria - LocalDate now {}", className, now);
    if (Boolean.TRUE.equals(filter.isToday())) {
//      LocalDateTime startOfDay = now.with(LocalTime.MIN);
//      LocalDateTime endOfDay = now.with(LocalTime.MAX);
//      criteriaList.add(Criteria.where("date.day")
//          .gte(startOfDay)
//          .lte(endOfDay));
      criteriaList.add(Criteria.where("date.day")
          .is(now.format(dateFormatter)));
      log.info("Class: {}, Method: addDateCriteria - used DateCriteria filter Today.", className);
    }

    if (Boolean.TRUE.equals(filter.isOnTheWeekend())) {
      LocalDate saturday = getNextSaturday(now);
//      LocalDate endOfWeekend = getNextSunday(now);

//      LocalDate saturday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
      LocalDate sunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

//      criteriaList.add(Criteria.where("date.day")
//          .gte(startOfWeekend.format(dateFormatter))
//          .lte(endOfWeekend.format(dateFormatter)));

      criteriaList.add(Criteria.where("date.day")
              .gte(saturday.format(dateFormatter))
              .lte(sunday.format(dateFormatter)));
      log.info("Class: {}, Method: addDateCriteria - used DateCriteria filter OnTheWeekend. ({}/{})",
          className, saturday, sunday);
    }

    if (Boolean.TRUE.equals(filter.isThisWeek())) {

      LocalDate endOfWeek = getNextSunday(now);
      criteriaList.add(Criteria.where("date.day")
          .gte(now.format(dateFormatter))
          .lte(endOfWeek.format(dateFormatter)));
      log.info("Class: {}, Method: addDateCriteria - used DateCriteria filter ThisWeek. ({}/{})",
          className, now, endOfWeek);

//      LocalDate startOfWeek = getPreviousMonday(now);
//      LocalDate endOfWeek = getNextSunday(now);
//      criteriaList.add(Criteria.where("date.day")
//              .gte(startOfWeek.format(dateFormatter))
//              .lte(endOfWeek.format(dateFormatter)));
//      log.info("Class: {}, Method: addDateCriteria - used DateCriteria filter ThisWeek. ({}/{})",
//              className, now, endOfWeek);
    }

//    if (filter.dateFrom() != null && filter.dateTo() != null) {
//      criteriaList.add(Criteria.where("date.day")
//          .gte(filter.dateFrom())
//          .lte(filter.dateTo()));
//    }
//    if (filter.today() != null && !filter.today().isEmpty() && isValidDate(filter.today())) {
//      criteriaList.add(Criteria.where("date.day")
//          .is(filter.today()));
//      log.info("Class: {}, Method: addDateCriteria - used DateCriteria filter today.", className);
//    }
//
//    if (filter.tomorrow() != null && !filter.tomorrow().isEmpty() && isValidDate(filter.tomorrow())) {
//      criteriaList.add(Criteria.where("date.day")
//          .is(filter.tomorrow()));
//      log.info("Class: {}, Method: addDateCriteria - used DateCriteria filter tomorrow.", className);
//    }
//
//    if (isValidDateRange(filter.thisWeek())) {
//      criteriaList.add(Criteria.where("date.day")
//          .gte(filter.thisWeek().startDay())
//          .lte(filter.thisWeek().endDay()));
//      log.info("Class: {}, Method: addDateCriteria - used DateCriteria filter thisWeek.", className);
//    }

    if (isValidDateRange(filter.dayRange())) {
      criteriaList.add(Criteria.where("date.day")
          .gte(filter.dayRange().startDay())
          .lte(filter.dayRange().endDay()));
      log.info("Class: {}, Method: addDateCriteria - used DateCriteria filter dayRange.", className);
    }
  }

  private void addPriceCriteria(EventFilterRequest filter, List<Criteria> criteriaList) {
    if (Boolean.TRUE.equals(filter.isFree())) {
      criteriaList.add(Criteria.where("ticketPrice").is(0));
      log.info("Class: {}, Method: addPriceCriteria - used ticketPrice filter isFree.", className);
    }

    if (Boolean.TRUE.equals(filter.isUnder500())) {
      criteriaList.add(Criteria.where("ticketPrice").gt(0).lte(499.0));
      log.info("Class: {}, Method: addPriceCriteria - used ticketPrice filter isUnder500.", className);
    }

    if (filter.priceRange() != null && filter.priceRange().priceFrom() != null && filter.priceRange().priceTo() != null) {
      criteriaList.add(Criteria.where("ticketPrice")
          .gte(filter.priceRange().priceFrom())
          .lte(filter.priceRange().priceTo()));
      log.info("Class: {}, Method: addPriceCriteria - used ticketPrice filter priceFrom - priceTo.", className);
    }
  }

  public static boolean isValidDate(String dateStr) {
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    try {
      LocalDate.parse(dateStr, DATE_FORMATTER);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  private boolean isValidDateRange(DateRange range) {
    return range != null
        && range.startDay() !=null
        && range.endDay() !=null
        && !range.startDay().isEmpty()
        && !range.endDay().isEmpty()
        && isValidDate(range.startDay())
        && isValidDate(range.endDay());
  }

  public LocalDate getNextSaturday(LocalDate date) {
    if(date.getDayOfWeek() == DayOfWeek.SUNDAY){
      return date;
    }
    return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
  }

  public LocalDate getNextSunday(LocalDate date) {
    return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
  }

  public LocalDate getPreviousMonday(LocalDate date) {
    return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
  }

  public LocalDate getCurrentDate() {
    return LocalDate.now(clock);
  }
}
