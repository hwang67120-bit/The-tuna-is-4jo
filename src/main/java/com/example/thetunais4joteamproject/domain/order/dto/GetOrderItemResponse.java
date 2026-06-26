package com.example.thetunais4joteamproject.domain.order.dto;

import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;

public record GetOrderItemResponse(
	Long orderItemId,
	Long productId,
	String productName,
	Long productOptionId,
	String optionName,
	Integer unitPrice,
	Integer quantity,
	Integer totalPrice
) {

	public static GetOrderItemResponse from(OrderItem orderItem) {
		return new GetOrderItemResponse(
			orderItem.getId(),
			orderItem.getProductId(),
			orderItem.getProductName(),
			orderItem.getProductOption().getId(),
			orderItem.getOptionName(),
			orderItem.getUnitPrice(),
			orderItem.getQuantity(),
			orderItem.getTotalPrice()
		);
	}
}
