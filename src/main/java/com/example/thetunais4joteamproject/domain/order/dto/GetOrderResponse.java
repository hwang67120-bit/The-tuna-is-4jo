package com.example.thetunais4joteamproject.domain.order.dto;

import java.time.LocalDateTime;

import com.example.thetunais4joteamproject.domain.order.entity.Order;

public record GetOrderResponse(
	Long orderId,
	String orderNumber,
	Integer orderPrice,
	Integer discountPrice,
	Integer deliveryPrice,
	Integer totalAmount,
	String orderStatus,
	LocalDateTime orderedAt
) {

	public static GetOrderResponse from(Order order) {
		return new GetOrderResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getOrderPrice(),
			order.getDiscountPrice(),
			order.getDeliveryPrice(),
			order.getTotalAmount(),
			order.getStatus().name(),
			order.getCreatedAt()
		);
	}
}
