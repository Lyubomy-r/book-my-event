package com.BookMyEvent.entity;

import com.BookMyEvent.entity.dto.FundsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "funds_request")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundsRequest {
    @Id
    private String id;
    private String cartNumber;
    private BigDecimal amount;
    private Instant creationDate;
    private Instant dateSendingFunds;
    private FundsStatus status;
    private String userId;
    private List<String> eventIds;
}
