package com.red.team.taskvisionapp.controller.exception;

import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionController {


    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CommonResponse<String>> validationExceptionHandler(
            ValidationException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse
                        .<String>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .error(exception.getMessage())
                        .build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CommonResponse<String>> responseStatusException(ResponseStatusException exception){
        return ResponseEntity.status(exception.getStatusCode())
                .body(CommonResponse
                        .<String>builder()
                        .statusCode(exception.getStatusCode().value())
                        .error(exception.getMessage())
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<String>> constraintViolationException(ConstraintViolationException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.<String>builder().error(exception.getMessage()).build());
    }


}

