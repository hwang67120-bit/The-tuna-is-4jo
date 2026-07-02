package com.example.thetunais4joteamproject.domain.order.dto;

import java.time.LocalDateTime;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

public record GetOrderResponse(
	Long orderId,
	String orderNumber,
	Integer orderPrice,
	Integer discountPrice,
	Integer deliveryPrice,
	Integer totalAmount,
	String orderStatus,
	String paymentStatus,
	LocalDateTime orderedAt
) {

	public static GetOrderResponse from(Order order, Payment payment) {
		return new GetOrderResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getOrderPrice(),
			order.getDiscountPrice(),
			order.getDeliveryPrice(),
			order.getTotalAmount(),
			order.getStatus().name(),
			payment.getStatus().name(),
			order.getCreatedAt()
		);
	}
}
