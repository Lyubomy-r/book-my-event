package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, ObjectId>, EventRepositoryCustom {

  @Query(value = "{ 'id': ?0 }", fields = "{ 'numberOfTickets': 1}")
  Optional<Event> findEventNumberOfTickets(String id);

  List<Event> findByDateDay(String day);

  @Query("{ 'date.day' : ?0 }")
  List<Event> findByEventStartDate(String day);

  Page<Event> findEventByEventStatus(EventStatus eventStatus, Pageable pageable);

  Page<Event> findEventByEventStatusAndLocation_City(
      EventStatus eventStatus, String city, Pageable pageable);

  Page<Event> findEventByEventCategory(EventCategory eventCategory, Pageable pageable);

  @Aggregation(
      pipeline = {"{ '$match': { 'eventCategory': ?0, 'eventStatus': ?1 }}", "{ '$sample': { 'size': ?2 } }"})
  List<Event> findRandomEventsByCategory(EventCategory category, EventStatus eventStatus, int size);

  @Aggregation(
      pipeline = {
        "{ '$match': { 'creationDate': { $gte: ?0, $lte: ?1 }, 'location.city': ?2, 'eventStatus': ?3 } }",
        "{ '$sample': { 'size': ?4 } }"
      })
  List<Event> findRandomEventsByCreationDateByCity(
      LocalDateTime fromDate,
      LocalDateTime toDate,
      String cityName,
      EventStatus eventStatus,
      int size);

  @Aggregation(
      pipeline = {
        "{ '$match': { 'creationDate': { $gte: ?0, $lte: ?1 }, 'eventStatus': ?2 } }",
        "{ '$sample': { 'size': ?3 } }"
      })
  List<Event> findRandomEventsByCreationDate(
      LocalDateTime fromDate, LocalDateTime toDate, EventStatus eventStatus, int size);

  List<Event> findEventByEventStatus(EventStatus eventStatus);

  List<Event> findByEventTypeIn(List<EventType> types);

  @Query(value = "{'organizers.id' : ?0 }", fields = "{ 'organizers': 0}")
  Page<Event> findByOrganizersId(ObjectId organizerId, Pageable pageable);

  List<Event> findByOrganizers_IdAndIsCompleted(ObjectId userId, boolean completed);
}
