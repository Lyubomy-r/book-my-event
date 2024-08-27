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



}
