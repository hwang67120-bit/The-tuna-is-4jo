package com.example.thetunais4joteamproject.domain.coupon.dto;

import java.time.LocalDateTime;

import com.example.thetunais4joteamproject.domain.coupon.entity.Coupon;

public record AvailableCouponResponse(
	Long couponId,
	String name,
	int discountPrice,
	int minOrderPrice,
	int totalQuantity,
	int remainingQuantity,
	LocalDateTime expirationAt,
	boolean issued
) {

	public static AvailableCouponResponse of(Coupon coupon, boolean issued) {
		return new AvailableCouponResponse(
			coupon.getId(),
			coupon.getName(),
			coupon.getDiscountPrice(),
			coupon.getMinOrderPrice(),
			coupon.getTotalQuantity(),
			coupon.getRemainingQuantity(),
			coupon.getExpirationAt(),
			issued
		);
	}
}
