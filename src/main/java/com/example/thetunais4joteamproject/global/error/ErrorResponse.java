package com.example.thetunais4joteamproject.global.error;

public record ErrorResponse(
	int status,
	String message
) {

	public static ErrorResponse from(ErrorCode errorCode) {
		return new ErrorResponse(
			errorCode.getHttpStatus().value(),
			errorCode.getMessage()
		);
	}
}
