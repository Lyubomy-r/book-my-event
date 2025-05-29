package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.EventUpdateRequest;
import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface EventUpdateRequestMapper {


  EventUpdateRequest toEventUpdateRequest(EventUpdateRequest updateRequest);
}
