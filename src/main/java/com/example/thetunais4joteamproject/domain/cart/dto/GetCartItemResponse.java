package com.example.thetunais4joteamproject.domain.cart.dto;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;

public record GetCartItemResponse(
	Long cartItemId,
	Long productId,
	String productName,
	Long productOptionId,
	String optionName,
	Integer unitPrice,
	Integer quantity,
	Integer totalPrice
) {

	public static GetCartItemResponse from(CartItem cartItem) {
		ProductOption productOption = cartItem.getProductOption();
		Product product = productOption.getProduct();

		int unitPrice = product.getPrice() + productOption.getAdditionalPrice();
		int totalPrice = unitPrice * cartItem.getQuantity();

		return new GetCartItemResponse(
			cartItem.getId(),
			product.getId(),
			product.getName(),
			productOption.getId(),
			productOption.getOptionName(),
			unitPrice,
			cartItem.getQuantity(),
			totalPrice
		);
	}
}