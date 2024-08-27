package com.BookMyEvent.service;

import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.dto.TicketResponseDto;

import java.util.List;

public interface TicketService {

  List<TicketResponseDto> findAllTicketByEventId(String eventId);

  List<TicketResponseDto> findAllTicketByUserId(String userId);

  TicketResponseDto buyTicket(Ticket ticket);

}
