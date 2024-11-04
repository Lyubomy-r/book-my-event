package com.BookMyEvent.service;

public interface AdminService {
    String banned(String email);

    String unbanned(String email);
}
