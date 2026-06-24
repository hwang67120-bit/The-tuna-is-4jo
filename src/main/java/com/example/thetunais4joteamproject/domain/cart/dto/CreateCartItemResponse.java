package com.example.thetunais4joteamproject.domain.cart.dto;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;

public record CreateCartItemResponse(
	Long cartItemId,
	Long productOptionId,
	Integer quantity
) {

	public static CreateCartItemResponse from(CartItem cartItem) {
		return new CreateCartItemResponse(
			cartItem.getId(),
			cartItem.getProductOption().getId(),
			cartItem.getQuantity()
		);
	}
}