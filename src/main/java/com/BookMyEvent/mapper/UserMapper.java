package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.exception.GeneralException;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface UserMapper {

  @Mapping(target = "id", expression = "java(convertToStringId(user.getId()))")
//  @Mapping(source = "user.email", target = "email")
//  @Mapping(source = "user.creationDate", target = "creationDate")
//  @Mapping(source = "user.location", target = "location")
//  @Mapping(source = "user.status", target = "status")
  @Mapping(target = "createdEvents", ignore = true)
  UserResponseDto toUserResponseDto(User user);

  @Mapping(target = "id", expression = "java(convertToStringId(user.getId()))")
  @Mapping(target = "avatarImage", ignore = true)
  @Mapping(target = "createdEvents", ignore = true)
  UserResponseDto toUserResponseDtoWithoutAvatarAndEvents(User user);

  //  @Mapping(source = "userSaveDto.name", target = "name")
//  @Mapping(source = "userSaveDto.email", target = "email")
//  @Mapping(source = "userSaveDto.password", target = "password")
//  @Mapping(source = "userSaveDto.phone", target = "phone")
  User toUserFromUserSaveDto(UserSaveDto userSaveDto);

//  @Mapping(target = "id", ignore = true)
//  @Mapping(target = "password", ignore = true)
//  User mapUserUpdateToUser(UserUpdateDto userUpdate, @MappingTarget User user);

  //  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(source = "user.id", target = "id")
  @Mapping(source = "user.mailConfirmation", target = "mailConfirmation")
  @Mapping(source = "user.role", target = "role")
  @Mapping(source = "user.creationDate", target = "creationDate")
  @Mapping(source = "user.avatarImage", target = "avatarImage")
  @Mapping(source = "user.location", target = "location")
  @Mapping(source = "user.status", target = "status")
  @Mapping(source = "user.createdEvents", target = "createdEvents")
  @Mapping(source = "user.email", target = "email")
  @Mapping(target = "name", expression = "java(checkAndAddNameIfExist(userUpdate, user))")
  @Mapping(target = "surname", expression = "java(checkAndAddSurNameIfExist(userUpdate, user))")
  @Mapping(target = "birthdayDate", expression = "java(checkAndAddBirthdayDateIfExist(userUpdate, user))")
  @Mapping(target = "phoneNumber", expression = "java(checkAndAddPhoneNumberIfExist(userUpdate, user))")
  User mapUserUpdateToUser(UserUpdateDto userUpdate, User user);

  @Mapping(target = "id", expression = "java(convertToStringId(event.getId()))")
  @Mapping(target = "organizers", ignore = true)
  EventResponseDto toEventResponseDto(Event event);

  List<EventResponseDto> toEventDTOList(List<Event> events);

  @Mapping(target = "createdEvents", source = "createdEvents")
  UserResponseDto toUserResponseDtoWithEvents(User user);

  @Mapping(target = "createdEvents", ignore = true)
  UserResponseDto toUserResponseDtoWithoutEvents(User user);

  default ObjectId convertToObjectId(String eventId) {
    return eventId != null && eventId.isEmpty() ? new ObjectId(eventId) : new ObjectId();
  }

  default String convertToStringId(ObjectId eventId) {
    return eventId != null ? eventId.toHexString() : null;
  }

  default User mapUserUpdateToUserWithPassword(UserUpdateDto userUpdateDto, User user, PasswordEncoder passwordEncoder) {
    User userw = mapUserUpdateToUser(userUpdateDto, user);
//    checkAndAddPasswordIfExist(userUpdateDto, userw, passwordEncoder);
    return userw;
  }

  default void checkAndAddPasswordIfExist(UserUpdateDto userUpdateDto, User user, PasswordEncoder passwordEncoder) {
    if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().isEmpty()
        && !passwordEncoder.matches(userUpdateDto.getPassword(), user.getPassword())) {
      user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
    }
  }

  default String checkAndAddNameIfExist(UserUpdateDto userUpdateDto, User user) {
    if (userUpdateDto.getName() != null && !userUpdateDto.getName().isEmpty()
        && !userUpdateDto.getName().equals(user.getName())) {
      return userUpdateDto.getName();
    } else {
      return user.getName();
    }
  }

  default String checkAndAddSurNameIfExist(UserUpdateDto userUpdateDto, User user) {
    if (userUpdateDto.getSurname() == null) {
      return user.getSurname();
    }
    if (userUpdateDto.getSurname().isEmpty()) {
      return null;
    }
    if (!userUpdateDto.getSurname().equals(user.getSurname())) {
      return userUpdateDto.getSurname();
    } else {
      return user.getSurname();
    }

//    return Optional.ofNullable(userUpdateDto.getSurname())
//        .map(surname -> surname.isEmpty() ? null : (surname.equals(user.getSurname()) ? user.getSurname() : surname))
//        .orElse(user.getSurname());
  }

  default String checkAndAddEmailIfExist(UserUpdateDto userUpdateDto, User user) {
      if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isEmpty()
          && !userUpdateDto.getEmail().equals(user.getEmail())) {
        return userUpdateDto.getEmail();
      } else {
        return user.getEmail();
      }
  }

  default String checkAndAddPhoneNumberIfExist(UserUpdateDto userUpdateDto, User user) {
    if (userUpdateDto.getPhoneNumber() == null) {
      return user.getPhoneNumber();
    }
    if (userUpdateDto.getPhoneNumber().isEmpty()) {
      return null;
    }
    if (!userUpdateDto.getPhoneNumber().equals(user.getPhoneNumber())) {
      return userUpdateDto.getPhoneNumber();
    } else {
      return user.getPhoneNumber();
    }
  }

  default LocalDate checkAndAddBirthdayDateIfExist(UserUpdateDto userUpdateDto, User user) {
    if (userUpdateDto.getBirthdayDate() == null) {
      return user.getBirthdayDate();
    }
    if (userUpdateDto.getBirthdayDate().isEmpty() ){
      return null;
    }
    try {
      DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      LocalDate updatedDate =  LocalDate.parse(userUpdateDto.getBirthdayDate(), FORMATTER);
      if (!updatedDate.equals(user.getBirthdayDate())) {
        return updatedDate;
      } else {
        return user.getBirthdayDate();
      }
    } catch (DateTimeParseException e) {
      throw new GeneralException("Incorrect date of birth", HttpStatus.BAD_REQUEST);
    }
  }
}
