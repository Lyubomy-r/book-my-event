package com.BookMyEvent.entity;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
@Slf4j
@Getter
public class CityList {
    private List<String> cityList;

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("file/City-List.txt");
        cityList = Files.readAllLines(resource.getFile().toPath());
        log.info("EventServiceImpl::init - Creating cityList size: {}", cityList.size());
    }

}
