package com.BookMyEvent.dao;

import com.BookMyEvent.entity.EventUpdateRequest;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventUpdateRequestRepository extends MongoRepository<EventUpdateRequest, String>, EventRepositoryCustom {

Optional<EventUpdateRequest> findEventUpdateRequestByEventId(String eventId);
}
