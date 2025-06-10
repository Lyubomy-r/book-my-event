package com.BookMyEvent.service;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.OrderDetailsDto;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.List;

public interface OrderDetailsService {

  List<OrderDetailsDto> findAllUserOrders(String userId);

  BigDecimal calculateTotalUserProfit(List<Event> listEvents);

  BigDecimal calculateUserProfitByEvent(ObjectId eventId);
}
