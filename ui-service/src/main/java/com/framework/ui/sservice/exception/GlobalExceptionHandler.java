package com.framework.ui.sservice.exception;

import com.framework.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleRestClientException(HttpStatusCodeException ex, HttpServletRequest request) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            Object error = objectMapper.readValue(responseBody, Object.class);
            return new ResponseEntity<>(error, ex.getStatusCode());
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(ex.getStatusCode().value())
                    .error(ex.getStatusCode().toString())
                    .message(ex.getMessage())
                    .path(request.getRequestURI())
                    .build();
            return new ResponseEntity<>(error, ex.getStatusCode());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
