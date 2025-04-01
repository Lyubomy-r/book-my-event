package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.EventFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventRepositoryCustom {
  Page<Event> filterEvents(EventFilterRequest filterRequest, Pageable pageable);
}
