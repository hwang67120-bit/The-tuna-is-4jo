package com.example.thetunais4joteamproject.global.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
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

    /**
     * 파일 업로드 용량 제한 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException exception
    ) {
        log.warn("File upload size exceeded: {}", exception.getMessage());
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * 최상위 예외 처리 (500 에러 포맷팅 보장)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception
    ) {
        log.error("Unhandled exception occurred", exception);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
}
