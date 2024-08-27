package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.TicketRepository;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.dto.TicketResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.TicketMapper;
import com.BookMyEvent.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImp implements TicketService {

  private final TicketRepository ticketRepository;

  private final EventRepository eventRepository;

  private final TicketMapper ticketMapper;


  @Override
  public List<TicketResponseDto> findAllTicketByEventId(String eventId) {
    ObjectId objectId = new ObjectId(eventId);
    List<TicketResponseDto> userList = ticketRepository.findAllByEventId(objectId);
    log.info("TicketServiceImp::findAllTicketByEventId. Return all the ticket sold from the event with the ID: {}", eventId);
    return userList;
  }

  @Override
  public List<TicketResponseDto> findAllTicketByUserId(String userId) {
    ObjectId objectId = new ObjectId(userId);
    List<TicketResponseDto> userList = ticketRepository.findAllByUserId(objectId);
    log.info("TicketServiceImp::findAllTicketByUserId. Return all the tickets purchased by the user with the ID {}", userId);
    return userList;
  }


  @Override
  public TicketResponseDto buyTicket(Ticket ticket) {
    log.warn("TicketServiceImp::buyTicket. Return error massage. {} ", ticket.getEventId());
    if (canWeBuyTicket(ticket.getEventId().toHexString(), ticket.getRow(), ticket.getSeat())) {
      log.warn("TicketServiceImp::buyTicket. Return error massage.");
      throw new GeneralException("There are no free places for the event. You can't buy ticket.", HttpStatus.BAD_REQUEST);
    }
    try {
      Ticket newTicket = ticketRepository.save(ticket);
      log.info("TicketServiceImp::buyTicket. Return saved Ticket by id: {}.", newTicket.getId());

      return ticketMapper.toUserResponseDto(newTicket);
    } catch (Exception e) {
      log.warn("TicketServiceImp::buyTicket. Return error massage : {}.", e.getMessage());
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public boolean canWeBuyTicket(String eventId, Long row, Long seat) {
    ObjectId objectId = new ObjectId(eventId);
    Optional<Event> numberOfTickets = eventRepository.findEventNumberOfTickets(eventId);
    boolean checkIfAreAvailablePlace = true;
    if (numberOfTickets.isPresent()) {
      log.info("TicketServiceImp::buyTicket. Return  String numberOfTickets : {}.",numberOfTickets.get());

      Long countByEventId = ticketRepository.countByEventId(objectId).orElse(0L);
      log.info("TicketServiceImp::buyTicket. Return  String countByEventId : {}.",countByEventId);
      checkIfAreAvailablePlace = (numberOfTickets.get().getNumberOfTickets() - countByEventId) > 0;
    }
    if (checkIfAreAvailablePlace) {

      boolean checkIfAreAvailableSeats = ticketRepository.existsByEventIdAndRowAndSeat(objectId, row, seat);
      log.info("TicketServiceImp::buyTicket. Return   checkIfAreAvailableSeats : {}.",checkIfAreAvailableSeats);
      return checkIfAreAvailableSeats;
    }
    return false;
  }


}
