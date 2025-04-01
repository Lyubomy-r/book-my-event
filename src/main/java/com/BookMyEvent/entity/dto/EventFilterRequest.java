package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.Enums.EventType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(name = "EventFilterRequest", description = "Filtering criteria for events")
public record EventFilterRequest(
     List<EventType> eventTypes,
     Boolean isNearby,
     Boolean isPopular,
//     LocalDate dateFrom,
//     LocalDate dateTo,
     Boolean isToday,
//     String today,
     Boolean isOnTheWeekend,
//     String tomorrow,
     Boolean isThisWeek,
//     DateRange thisWeek,
     DateRange dayRange,
     Boolean isFree,
     Boolean isUnder500,
     PriceRange priceRange,
     String latitude,
     String longitude
) {
}
