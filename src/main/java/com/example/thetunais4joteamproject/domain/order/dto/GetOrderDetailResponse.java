package com.example.thetunais4joteamproject.domain.order.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;

public record GetOrderDetailResponse(
	Long orderId,
	String orderNumber,
	Integer orderPrice,
	Integer discountPrice,
	Integer deliveryPrice,
	Integer totalAmount,
	String orderStatus,
	LocalDateTime orderedAt,
	List<GetOrderItemResponse> items
) {

	public static GetOrderDetailResponse of(Order order, List<OrderItem> orderItems) {
		List<GetOrderItemResponse> items = new ArrayList<>();

		for (OrderItem orderItem : orderItems) {
			items.add(GetOrderItemResponse.from(orderItem));
		}

		return new GetOrderDetailResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getOrderPrice(),
			order.getDiscountPrice(),
			order.getDeliveryPrice(),
			order.getTotalAmount(),
			order.getStatus().name(),
			order.getCreatedAt(),
			items
		);
	}
}
