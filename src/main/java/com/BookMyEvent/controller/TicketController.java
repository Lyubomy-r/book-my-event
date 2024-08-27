package com.BookMyEvent.controller;

import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.dto.TicketResponseDto;
import com.BookMyEvent.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

  private final TicketService ticketService;

  @GetMapping("/event/{eventId}")
  public ResponseEntity<List<TicketResponseDto>> findAllTicketByEventId(@PathVariable("eventId") String eventId) {
    return ResponseEntity.ok(ticketService.findAllTicketByEventId(eventId));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<TicketResponseDto>> findAllTicketByUserId(@PathVariable("userId") String userId) {
    return ResponseEntity.ok(ticketService.findAllTicketByUserId(userId));
  }

  @PostMapping()
  public ResponseEntity<TicketResponseDto> save(@RequestBody Ticket ticket) {
    return ResponseEntity.ok(ticketService.buyTicket(ticket));
  }
}
