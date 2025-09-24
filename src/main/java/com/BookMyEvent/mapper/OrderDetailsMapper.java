package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.PaymentDetails;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.OrderDetailsDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, EventMapper.class, ObjectIdMapper.class},
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface OrderDetailsMapper {
  @Mapping(target = "id", expression = "java(asString(orderDetails.getId()))")
  @Mapping(target = "productCount", expression = "java(getProductCount(orderDetails))")
  @Mapping(target = "amount", expression = "java(getAmount(orderDetails))")
  @Mapping(target = "event", source = "event", qualifiedByName = "mapToEventResponseDto")
  @Mapping(target = "user", source = "user", qualifiedByName = "mapUserToDtoWithoutAvatarAndEvents")
  OrderDetailsDto newOrderDetails(OrderDetails orderDetails);


  @Mapping(target = "id", ignore = true)
  @Mapping(target = "reservationExpires", ignore = true)
  @Mapping(target = "row", ignore = true)
  @Mapping(target = "seat", ignore = true)
  @Mapping(target = "status", constant = "PAID")
  @Mapping(target = "orderReference", source = "orderReference")
  @Mapping(target = "orderDate", source = "orderDateInInstant")
  @Mapping(target = "event", source = "savedEvent")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "paymentDetails", source = "paymentDetails")
  OrderDetails toOrderDetailsForFreePurchase(String orderReference,
                              Instant orderDateInInstant,
                              Event savedEvent,
                              User user,
                              PaymentDetails paymentDetails);

  default String asString(ObjectId id) {
    return id != null ? id.toHexString() : null;
  }

  default String getProductCount(OrderDetails orderDetails) {
    return orderDetails.getPaymentDetails().getProduct().productCount() != null
        ? orderDetails.getPaymentDetails().getProduct().productCount()
        : null;
  }

  default String getAmount(OrderDetails orderDetails) {
    return orderDetails.getPaymentDetails().getProduct().amount() != null
        ? orderDetails.getPaymentDetails().getProduct().amount()
        : null;
  }
}
