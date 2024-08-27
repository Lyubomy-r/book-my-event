package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.dto.TicketResponseDto;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {

  List<TicketResponseDto> findAllByEventId(ObjectId eventId);

  List<TicketResponseDto> findAllByUserId(ObjectId userId);

  @Query(value = "{ 'eventId': ?0 }", count = true)
  Optional<Long> countByEventId(ObjectId eventId);

  boolean existsByEventIdAndRowAndSeat(ObjectId eventId, Long row, Long seat);
}
