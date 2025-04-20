package com.BookMyEvent.dao;

import com.BookMyEvent.entity.PromoCode;
import com.BookMyEvent.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoCodeRepository extends MongoRepository<PromoCode, ObjectId> {

  Optional<PromoCode> findByName(String promoCodeName);
}
