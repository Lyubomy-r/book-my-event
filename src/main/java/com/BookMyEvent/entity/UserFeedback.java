package com.BookMyEvent.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFeedback {
    @Id
    private String id;
    @NotBlank(message = "Email користувача не може бути порожнім")
    private String userEmail;
    @NotBlank(message = "Повідомлення не може бути порожнім")
    private String feedbackMessage;
}
