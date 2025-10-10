package com.BookMyEvent.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class AppController {

    @GetMapping("/heals")
    public ResponseEntity<?>  checkServerHeals(){
        return ResponseEntity.ok().build();
    }
}
