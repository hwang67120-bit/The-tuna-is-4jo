package com.example.thetunais4joteamproject.domain.refund.entity;

import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

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

	public boolean canTransitionTo(RefundStatus nextStatus) {
		return switch (this) {
			case REQUESTED -> nextStatus == COMPLETED ||
				nextStatus == REJECTED ||
				nextStatus == FAILED;

			case REJECTED, COMPLETED, FAILED -> false;
		};
	}

	public void validateTransition(RefundStatus nextStatus) {
		if (!canTransitionTo(nextStatus)) {
			throw BusinessException.from(ErrorCode.INVALID_REFUND_STATUS_TRANSITION);
		}
	}
}