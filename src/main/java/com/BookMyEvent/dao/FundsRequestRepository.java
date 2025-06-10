package com.BookMyEvent.dao;

import com.BookMyEvent.entity.FundsRequest;
import com.BookMyEvent.entity.dto.FundsStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundsRequestRepository extends MongoRepository<FundsRequest, String> {
    List<FundsRequest> findByUserId(String userId);

    List<FundsRequest> findByUserIdAndStatusIn(String organizerId, List<FundsStatus> statuses);
}
