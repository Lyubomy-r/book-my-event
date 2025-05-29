package com.BookMyEvent.dao;

import com.BookMyEvent.entity.UserFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserFeedbackRepository extends MongoRepository<UserFeedback, String> {

}
