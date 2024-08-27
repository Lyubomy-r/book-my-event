package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Event;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

  @Query(value = "{ 'id': ?0 }", fields = "{ 'numberOfTickets': 1}")
  Optional<Event> findEventNumberOfTickets(String id);


}
