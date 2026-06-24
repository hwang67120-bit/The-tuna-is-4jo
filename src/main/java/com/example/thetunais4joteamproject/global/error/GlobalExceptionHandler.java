package com.example.thetunais4joteamproject.global.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException businessException
    ) {
        ErrorCode errorCode = businessException.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException() {
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException() {
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
}
