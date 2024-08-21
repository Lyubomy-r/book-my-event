package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Events;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Events, String> {
}
