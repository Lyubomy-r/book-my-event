package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.UserFeedbackRepository;
import com.BookMyEvent.entity.UserFeedback;
import com.BookMyEvent.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImp implements FeedbackService {
    private final UserFeedbackRepository userFeedbackRepository;
    private final String className = this.getClass().getSimpleName();

    @Override
    public String saveFeedback(UserFeedback userFeedback){
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        userFeedbackRepository.save(userFeedback);
        log.info("{}::{} - Feedback  saved successfully.", className, methodName);
        return "Your feedback has been saved successfully";
    }

    @Override
    public List<UserFeedback> findAllFeedback(){
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        List<UserFeedback> feedbackList = userFeedbackRepository.findAll();
        log.info("{}::{} - find all Feedback ({}).", className, methodName, feedbackList.size());
        return feedbackList;
    }

}
