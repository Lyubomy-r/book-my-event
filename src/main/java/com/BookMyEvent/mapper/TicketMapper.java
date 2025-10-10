package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.dto.TicketDto;
import com.BookMyEvent.entity.dto.TicketResponseDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(
    componentModel = "spring",
    uses = {ObjectIdMapper.class},
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface TicketMapper {
  //  TicketMapper INSTANCE = Mappers.getMapper(TicketMapper.class);
//  @Mapping(target = "buyingDate", expression = "java(java.time.LocalDateTime.now())")
//  @Mapping(
//      target = "reservationExpires",
//      expression = "java(java.time.LocalDateTime.now().plusMinutes(15))")
//  @Mapping(target = "eventId", source = "eventId", qualifiedByName = "stringToObjectId")
//  @Mapping(target = "userId", source = "userId", qualifiedByName = "stringToObjectId")
//  Ticket toTicket(String eventId, String userId, int numberOfTickets, Long row, Long seat);

  //  @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
  //  @Mapping(target = "buyingDate", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "status", expression = "java(mapStatusToStringUa(ticket))")
  @Mapping(target = "orderDate", expression = "java(mapOrderDateToStringKyivZone(ticket))")
  //  @Mapping(target = "userId", source = "userId", qualifiedByName = "stringToObjectId")
  TicketDto toTicketDto(Ticket ticket);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "title", source = "orderDetails.event.title")
  @Mapping(target = "ticketPrice", source = "orderDetails.event.ticketPrice")
  @Mapping(target = "ticketReference", source = "reference")
  @Mapping(target = "date", source = "orderDetails.event.date")
//  @Mapping(target = "row", source = "orderDetails.row")
//  @Mapping(target = "seat", source = "orderDetails.seat")
  @Mapping(target = "status", source = "orderDetails.status")
  @Mapping(target = "location", source = "orderDetails.event.location")
  @Mapping(target = "images", source = "orderDetails.event.images")
  @Mapping(target = "eventUrl", source = "orderDetails.event.eventUrl")
  @Mapping(target = "eventFormat", expression = "java(orderDetails.getEvent().getEventFormat() != null ? orderDetails.getEvent().getEventFormat().name() : null)")
  @Mapping(target = "orderDate", source = "orderDetails.orderDate")
  @Mapping(target = "orderDetailsId", expression = "java(orderDetails.getId() != null ? orderDetails.getId().toString() : null)")
  @Mapping(target = "eventId", expression = "java(orderDetails.getEvent().getId() != null ? orderDetails.getEvent().getId().toString() : null)")
  @Mapping(target = "userId", expression = "java(orderDetails.getUser() != null && orderDetails.getUser().getId() != null ? orderDetails.getUser().getId().toString() : null)")
  Ticket mapToTicketAfterPay(OrderDetails orderDetails, String reference);

  //  @Mapping(source = "ticket.id", target = "id")
  //  @Mapping(source = "ticket.title", target = "title")
  //  @Mapping(source = "ticket.description", target = "description")
  //  @Mapping(
  //      target = "eventId",
  //      expression = "java(ticket.getEventId() != null ? ticket.getEventId().toHexString() :
  // null)")
  //  @Mapping(
  //      target = "userId",
  //      expression = "java(ticket.getUserId() != null ? ticket.getUserId().toHexString() : null)")
  //  @Mapping(source = "ticket.startDate", target = "startDate")
  //  @Mapping(source = "ticket.startTime", target = "startTime")
  //  @Mapping(source = "ticket.buyingDate", target = "buyingDate")
  //  @Mapping(source = "ticket.reservationExpires", target = "reservationExpires")
  //  @Mapping(source = "ticket.row", target = "row")
  //  @Mapping(source = "ticket.seat", target = "seat")
  //  @Mapping(source = "ticket.ticketPrice", target = "ticketPrice")
  //  @Mapping(source = "ticket.numberOfTickets", target = "numberOfTickets")
  //  @Mapping(source = "ticket.location", target = "location")
  //  TicketResponseDto toUserResponseDto(Ticket ticket);

  @Named("stringToObjectId")
  default ObjectId mapStringToObjectId(String id) {
    return (id != null && !id.isEmpty()) ? new ObjectId(id) : null;
  }

  default String mapStatusToStringUa(Ticket ticket) {
    if (ticket != null && ticket.getStatus() != null) {
      return ticket.getStatus().getNameUa();
    }
    return null;
  }

  default String mapOrderDateToStringKyivZone(Ticket ticket) {
    ZoneId kyivZone = ZoneId.of("Europe/Kiev");
    ZonedDateTime kyivTime = ticket.getOrderDate().atZone(kyivZone);
    return kyivTime.toString();
  }
}
