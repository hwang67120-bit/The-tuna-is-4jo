package com.example.thetunais4joteamproject.domain.order.dto;

import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;

public record CreateOrderItemResponse(
	Long orderItemId,
	Long productId,
	String productName,
	Long productOptionId,
	String optionName,
	Integer unitPrice,
	Integer quantity,
	Integer totalPrice
) {

	public static CreateOrderItemResponse from(OrderItem orderItem) {
		return new CreateOrderItemResponse(
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