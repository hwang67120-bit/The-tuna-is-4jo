package com.example.thetunais4joteamproject.domain.refund.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundStatus {

	REQUESTED("환불 요청"),
	REJECTED("환불 거절"),
	COMPLETED("환불 완료"),
	FAILED("환불 실패");

	private final String message;
}