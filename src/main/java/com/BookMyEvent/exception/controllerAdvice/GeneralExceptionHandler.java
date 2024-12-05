package com.BookMyEvent.exception.controllerAdvice;

import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class GeneralExceptionHandler {

  @ExceptionHandler(GeneralException.class)
  public ResponseEntity<ErrorResponseDto> handleGeneralException(GeneralException exception) {

    ErrorResponseDto errorResponse = new ErrorResponseDto(exception.getHttpStatus().value(), exception.getMessage());
    log.info("From GeneralExceptionHandler::handleGeneralException. Send message error ({})", exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.OK);
  }



  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );
    return errors;
  }
}
