package com.example.thetunais4joteamproject.domain.order.dto;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

public record CancelOrderResponse(
	Long orderId,
	String orderNumber,
	Integer totalAmount,
	String orderStatus,
	String paymentStatus
) {

	public static CancelOrderResponse of(Order order, Payment payment) {
		return new CancelOrderResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getTotalAmount(),
			order.getStatus().name(),
			payment.getStatus().name()
		);
	}
}
