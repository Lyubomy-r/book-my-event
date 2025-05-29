package com.BookMyEvent.dao;

import com.BookMyEvent.entity.EventDeleteRequest;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventDeleteRequestRepository extends MongoRepository<EventDeleteRequest, ObjectId>, EventRepositoryCustom {
  Optional<EventDeleteRequest> findByEventId(String eventId);

  boolean existsByEventId(String eventId);
}
