package com.example.thetunais4joteamproject.domain.order.entity;

import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

	PENDING_PAYMENT("결제 대기"),
	CONFIRMED("주문 확정"),
	CANCELED("주문 취소");

	private final String message;

	public boolean canTransitionTo(OrderStatus nextStatus) {
		return switch (this) {
			case PENDING_PAYMENT -> nextStatus == CONFIRMED ||
				nextStatus == CANCELED;

			case CONFIRMED -> nextStatus == CANCELED;

			case CANCELED -> false;
		};
	}

	public void validateTransition(OrderStatus nextStatus) {
		if (nextStatus == null || !canTransitionTo(nextStatus)) {
			throw BusinessException.from(ErrorCode.INVALID_ORDER_STATUS);
		}
	}
}