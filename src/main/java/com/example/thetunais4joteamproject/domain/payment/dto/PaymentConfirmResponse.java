package com.example.thetunais4joteamproject.domain.payment.dto;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

public record PaymentConfirmResponse(
	Long paymentId,
	Long orderId,
	int amount,
	String paymentStatus,
	String orderStatus,
	String message
) {
	public static PaymentConfirmResponse of(
		Payment payment,
		String message
	) {
		return new PaymentConfirmResponse(
			payment.getId(),
			payment.getOrder().getId(),
			payment.getPgAmount(),
			payment.getStatus().name(),
			payment.getOrder().getStatus().name(),
			message
		);
	}
}
