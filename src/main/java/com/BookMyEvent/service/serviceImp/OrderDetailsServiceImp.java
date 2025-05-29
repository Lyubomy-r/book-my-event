package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.OrderDetailsRepository;
import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.OrderDetailsDto;
import com.BookMyEvent.service.OrderDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

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
    public List<OrderDetailsDto> findAllUserOrders(String userId){
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        ObjectId objectId = new ObjectId(userId);
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findByUser_Id(objectId);
        List<OrderDetailsDto> response = orderDetailsList.stream().map(order-> {
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
            return new OrderDetailsDto(order.getId().toHexString(), order.getOrderReference(), kyivTime.toString(), order.getPaymentDetails().getProduct().productCount(), order.getPaymentDetails().getProduct().productCount(),
                    eventResponseDto, order.getStatus().getNameUa());
        }).toList();
        log.info("{}::{} - find all user orders return : {} orders.", className, methodName, orderDetailsList.size());
        return response;
    }



}
