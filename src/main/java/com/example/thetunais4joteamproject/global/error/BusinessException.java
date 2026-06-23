package com.example.thetunais4joteamproject.global.error;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    private BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static BusinessException from(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
