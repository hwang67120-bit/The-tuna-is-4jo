package com.example.thetunais4joteamproject.domain.cart.dto;

import java.util.List;

public record GetCartResponse(
	List<GetCartItemResponse> items,
	Integer totalPrice
) {

	public static GetCartResponse of(List<GetCartItemResponse> items) {
		int totalPrice = 0;

		for (GetCartItemResponse item : items) {
			totalPrice += item.totalPrice();
		}

		return new GetCartResponse(items, totalPrice);
	}

	public static GetCartResponse empty() {
		return new GetCartResponse(List.of(), 0);
	}
}