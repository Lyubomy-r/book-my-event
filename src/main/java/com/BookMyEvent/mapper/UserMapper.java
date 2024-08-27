package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface UserMapper {

  @Mapping(target = "id", expression = "java(user.getId() != null ? user.getId().toHexString() : null)")
  @Mapping(source = "user.name", target = "name")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.creationDate", target = "creationDate")
  @Mapping(source = "user.location", target = "location")
  @Mapping(source = "user.status", target = "status")
  UserResponseDto toUserResponseDto(User user);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  void mapUserUpdateToUser(UserUpdateDto userUpdate, @MappingTarget User user);
}
