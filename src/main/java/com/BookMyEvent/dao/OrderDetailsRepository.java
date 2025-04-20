package com.BookMyEvent.dao;

import com.BookMyEvent.entity.OrderDetails;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderDetailsRepository extends MongoRepository<OrderDetails, ObjectId> {

  Optional<OrderDetails> findByOrderReference(String orderReference);

  List<OrderDetails> findByEvent_Id(ObjectId eventId);
}
