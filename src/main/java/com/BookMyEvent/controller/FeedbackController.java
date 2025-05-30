package com.BookMyEvent.controller;

import com.BookMyEvent.entity.UserFeedback;
import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Feedback Endpoints")
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final String className = this.getClass().getSimpleName();

    @PostMapping
    public ResponseEntity<AppResponse> saveFeedback(@RequestBody UserFeedback userFeedback){
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("Class: {}, Method: {} - get request.", className, methodName);
        String responseMessage = feedbackService.saveFeedback(userFeedback);
        AppResponse response = new AppResponse(200, responseMessage);
        log.info("Class: {}, Method: {} - return {}", className, methodName, response);
        return ResponseEntity.ok(response);
    }
}
