package com.BookMyEvent.service;

import com.BookMyEvent.entity.UserFeedback;

import java.util.List;

public interface FeedbackService {
    String saveFeedback(UserFeedback userFeedback);

    List<UserFeedback> findAllFeedback();
}
