package com.example.thetunais4joteamproject.domain.order.dto;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;

public record OrderPreviewItemResponse(
	Long cartItemId,
	Long productId,
	String productName,
	Long productOptionId,
	String optionName,
	Integer unitPrice,
	Integer quantity,
	Integer totalPrice
) {

	public static OrderPreviewItemResponse from(CartItem cartItem) {
		ProductOption productOption = cartItem.getProductOption();
		Product product = productOption.getProduct();

		int unitPrice = product.getPrice() + productOption.getAdditionalPrice();
		int totalPrice = unitPrice * cartItem.getQuantity();

		return new OrderPreviewItemResponse(
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