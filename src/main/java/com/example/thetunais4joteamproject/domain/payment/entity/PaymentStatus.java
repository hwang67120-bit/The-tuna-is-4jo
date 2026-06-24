package com.example.thetunais4joteamproject.domain.payment.entity;

import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

	PENDING("결제 대기"),
	PAID("결제 완료"),
	FAILED("결제 실패"),
	CANCELED("결제 취소"),
	REFUNDED("결제 환불");

	private final String message;

	public boolean canTransitionTo(PaymentStatus nextStatus) {
		return switch (this) {
			case PENDING -> nextStatus == PAID ||
				nextStatus == FAILED ||
				nextStatus == CANCELED;

			case PAID -> nextStatus == REFUNDED;

			case FAILED, CANCELED, REFUNDED -> false;
		};
	}

	public void validateTransition(PaymentStatus nextStatus) {
		if (nextStatus == null || !canTransitionTo(nextStatus)) {
			throw BusinessException.from(ErrorCode.PAYMENT_INVALID_STATUS);
		}
	}
}