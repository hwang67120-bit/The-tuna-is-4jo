package com.example.thetunais4joteamproject.domain.order.dto;

public record CreateDirectOrderRequest(
	Long productOptionId,
	Integer quantity,
	Long memberCouponId,
	Long memberAddressId
) {

	public CreateDirectOrderRequest(Long productOptionId, Integer quantity, Long memberCouponId) {
		this(productOptionId, quantity, memberCouponId, null);
	}
}
