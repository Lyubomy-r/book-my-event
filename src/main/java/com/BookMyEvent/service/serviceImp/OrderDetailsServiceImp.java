package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.OrderDetailsRepository;
import com.BookMyEvent.entity.Enums.OrderStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.OrderDetailsDto;
import com.BookMyEvent.service.OrderDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDetailsServiceImp implements OrderDetailsService {
  private final OrderDetailsRepository orderDetailsRepository;

  private final String className = this.getClass().getSimpleName();

  @Override
  public List<OrderDetailsDto> findAllUserOrders(String userId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    ObjectId objectId = new ObjectId(userId);
    List<OrderDetails> orderDetailsList = orderDetailsRepository.findByUser_Id(objectId);
    List<OrderDetailsDto> response =
        orderDetailsList.stream()
            .map(
                order -> {
                  ZoneId kyivZone = ZoneId.of("Europe/Kiev");
                  ZonedDateTime kyivTime = order.getOrderDate().atZone(kyivZone);
                  //            String orderDate = String.valueOf(kyivTime.toEpochSecond());
                  EventResponseDto eventResponseDto = new EventResponseDto();
                  eventResponseDto.setId(order.getEvent().getId().toHexString());
                  eventResponseDto.setTitle(order.getEvent().getTitle());
                  eventResponseDto.setDate(order.getEvent().getDate());
                  eventResponseDto.setTicketPrice(order.getEvent().getTicketPrice());
                  eventResponseDto.setTitle(order.getEvent().getTitle());
                  eventResponseDto.setLocation(order.getEvent().getLocation());
                  return new OrderDetailsDto(
                      order.getId().toHexString(),
                      order.getOrderReference(),
                      kyivTime.toString(),
                      order.getPaymentDetails().getProduct().productCount(),
                      order.getPaymentDetails().getProduct().productCount(),
                      eventResponseDto,
                      order.getStatus().getNameUa());
                })
            .toList();
    log.info(
        "{}::{} - find all user orders return : {} orders.",
        className,
        methodName,
        orderDetailsList.size());
    return response;
  }

  @Override
  public BigDecimal calculateTotalUserProfit(List<Event> listEvents) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    BigDecimal totalAmount =
        listEvents.stream()
            .map(
                event ->
                        calculateUserProfitByEvent(event.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    log.info("{}::{} - calculate the total user profit by all events : {} .", className, methodName, totalAmount);

    return totalAmount.setScale(2, RoundingMode.DOWN);
  }

  @Override
  public BigDecimal calculateUserProfitByEvent(ObjectId eventId) {
    String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
    List<OrderDetails> orderDetailsList =
        orderDetailsRepository.findByEvent_IdAndStatus(eventId, OrderStatus.PAID);
    BigDecimal response =
        orderDetailsList.stream()
            .map(
                order -> {
                  double doubleAmount =
                      Double.parseDouble(order.getPaymentDetails().getProduct().productPrice());
                  return BigDecimal.valueOf(doubleAmount);
                })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    log.info("{}::{} - calculate user profit by one event : {} .", className, methodName, response);

    return response;
  }
}
