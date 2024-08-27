package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.dto.TicketResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface TicketMapper {

  @Mapping(source = "ticket.id", target = "id")
  @Mapping(source = "ticket.title", target = "title")
  @Mapping(source = "ticket.description", target = "description")
  @Mapping(target = "eventId", expression = "java(ticket.getEventId() != null ? ticket.getEventId().toHexString() : null)")
  @Mapping(target = "userId", expression = "java(ticket.getUserId() != null ? ticket.getUserId().toHexString() : null)")
  @Mapping(source = "ticket.startDate", target = "startDate")
  @Mapping(source = "ticket.startTime", target = "startTime")
  @Mapping(source = "ticket.buyingDate", target = "buyingDate")
  @Mapping(source = "ticket.row", target = "row")
  @Mapping(source = "ticket.seat", target = "seat")
  @Mapping(source = "ticket.ticketPrice", target = "ticketPrice")
  @Mapping(source = "ticket.location", target = "location")
  TicketResponseDto toUserResponseDto(Ticket ticket);
}
