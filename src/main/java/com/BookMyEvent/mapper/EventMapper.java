package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "id", source = "eventDTO.id")
    @Mapping(target = "title", source = "eventDTO.title")
    @Mapping(target = "description", source = "eventDTO.description")
    @Mapping(target = "photoUrl", source = "eventDTO.photoUrl")
    @Mapping(target = "eventStartDate", source = "eventDTO.eventStartDate")
    @Mapping(target = "phoneNumber", source = "eventDTO.phoneNumber")
    @Mapping(target = "ticketPrice", source = "eventDTO.ticketPrice")
    @Mapping(target = "numberOfTickets", source = "eventDTO.numberOfTickets")
    @Mapping(target = "availableTickets", source = "eventDTO.availableTickets")
    @Mapping(target = "location", source = "eventDTO.location")
    @Mapping(target = "organizers", source = "eventDTO.organizers")
    Event toEvent(EventDTO eventDTO);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "title", source = "event.title")
    @Mapping(target = "description", source = "event.description")
    @Mapping(target = "photoUrl", source = "event.photoUrl")
    @Mapping(target = "eventStartDate", source = "event.eventStartDate")
    @Mapping(target = "phoneNumber", source = "event.phoneNumber")
    @Mapping(target = "ticketPrice", source = "event.ticketPrice")
    @Mapping(target = "numberOfTickets", source = "event.numberOfTickets")
    @Mapping(target = "availableTickets", source = "event.availableTickets")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "organizers", source = "event.organizers")

    EventDTO toEventDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)

    void updateEventFromDTO(EventDTO eventDTO, @MappingTarget Event event);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "title", source = "event.title")
    @Mapping(target = "description", source = "event.description")
    @Mapping(target = "photoUrl", source = "event.photoUrl")
    @Mapping(target = "date", source = "event.date")
    @Mapping(target = "phoneNumber", source = "event.phoneNumber")
    @Mapping(target = "ticketPrice", source = "event.ticketPrice")
    @Mapping(target = "numberOfTickets", source = "event.numberOfTickets")
    @Mapping(target = "availableTickets", source = "event.availableTickets")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "organizers", source = "event.organizers")
    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().getUkrainianName() : null)")
    @Mapping(target = "eventCategory", expression = "java(event.getEventCategory() != null ? event.getEventCategory().getUkrainianName() : null)")
    EventResponseDto toEventResponseDtoFromEvent(Event event);
}
