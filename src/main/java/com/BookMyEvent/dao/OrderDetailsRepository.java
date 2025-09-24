package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Enums.OrderStatus;
import com.BookMyEvent.entity.OrderDetails;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderDetailsRepository extends MongoRepository<OrderDetails, ObjectId> {

  Optional<OrderDetails> findByOrderReference(String orderReference);

  List<OrderDetails> findByEvent_IdAndStatus(ObjectId eventId, OrderStatus status);

  Page<OrderDetails> findByUser_Id(ObjectId eventId, Pageable pageable);
}
