package com.BookMyEvent.dao;

import com.BookMyEvent.entity.EventCancelRequest;
import com.BookMyEvent.entity.EventUpdateRequest;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventCancelRequestRepository extends MongoRepository<EventCancelRequest, ObjectId>, EventRepositoryCustom {
Optional<EventCancelRequest> findEventCancelRequestByEventId(String eventId);
}
