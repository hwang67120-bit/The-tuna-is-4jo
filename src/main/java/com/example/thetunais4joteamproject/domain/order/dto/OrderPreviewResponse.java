package com.example.thetunais4joteamproject.domain.order.dto;

import java.util.List;

public record OrderPreviewResponse(
	List<OrderPreviewItemResponse> items,
	Integer orderPrice,
	Integer discountPrice,
	Integer deliveryPrice,
	Integer paymentPrice
) {

	public static OrderPreviewResponse of(
		List<OrderPreviewItemResponse> items,
		Integer discountPrice,
		Integer deliveryPrice
	) {
		int orderPrice = 0;

		for (OrderPreviewItemResponse item : items) {
			orderPrice += item.totalPrice();
		}

		int paymentPrice = orderPrice - discountPrice + deliveryPrice;

		return new OrderPreviewResponse(
			items,
			orderPrice,
			discountPrice,
			deliveryPrice,
			paymentPrice
		);
	}
}