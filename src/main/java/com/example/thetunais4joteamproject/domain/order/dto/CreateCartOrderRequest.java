package com.example.thetunais4joteamproject.domain.order.dto;

import java.util.List;

public record CreateCartOrderRequest(
	List<Long> cartItemIds,
	Long memberCouponId,
	Long memberAddressId
) {

	public CreateCartOrderRequest(List<Long> cartItemIds, Long memberCouponId) {
		this(cartItemIds, memberCouponId, null);
	}
}
