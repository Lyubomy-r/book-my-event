package com.BookMyEvent.service;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.OrderDetailsDto;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OrderDetailsService {

  Page<OrderDetailsDto> findAllUserOrders(String userId, Pageable pageable);

  BigDecimal calculateTotalUserProfit(List<Event> listEvents);

  BigDecimal calculateUserProfitByEvent(ObjectId eventId);
}
