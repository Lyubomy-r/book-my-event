package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

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

  List<Event> findEventByEventStatus(EventStatus eventStatus);

  List<Event> findByEventTypeIn(List<EventType> types);

  @Query(value = "{'organizers.id' : ?0 }", fields = "{ 'organizers': 0}")
  Page<Event> findByOrganizersId(ObjectId organizerId, Pageable pageable);

}
