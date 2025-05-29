package com.BookMyEvent.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class FieldValidationException extends GeneralException {
  private Map<String, String> details;

  public FieldValidationException(String message, Map<String, String> details, HttpStatus httpStatus) {
    super(message,httpStatus);
    this.details=details;
  }
}
