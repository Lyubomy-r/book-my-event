package com.BookMyEvent.service;


import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.dto.OrderDetailsDto;

import java.util.List;

public interface OrderDetailsService {

    List<OrderDetailsDto> findAllUserOrders(String userId);
}
