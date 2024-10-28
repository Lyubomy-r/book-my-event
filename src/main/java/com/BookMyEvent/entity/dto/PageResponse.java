package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class PageResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ssa")
    private LocalDateTime timestamp;

    @JsonProperty("statusСode")
    private Integer statusCode;

    private List<User> users;
    private int totalPages;
    private long totalElements;
    public PageResponse(Integer statusCode, List<User> users, int totalPages, long totalElements ) {
        this.timestamp = LocalDateTime.now();
        this.statusCode = statusCode;
        this.users = users;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
