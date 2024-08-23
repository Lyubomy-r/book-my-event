package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface UserMapper {

  @Mapping(source = "user.id", target = "id")
  @Mapping(source = "user.name", target = "name")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.creationDate", target = "creationDate")
  @Mapping(source = "user.location", target = "location")
  UserResponseDto toUserResponseDto(User user);
}
