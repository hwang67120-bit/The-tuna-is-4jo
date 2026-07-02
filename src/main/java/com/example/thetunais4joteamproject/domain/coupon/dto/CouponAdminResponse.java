package com.example.thetunais4joteamproject.domain.coupon.dto;

import java.time.LocalDateTime;

public record CouponAdminResponse(
	Long couponId,
	String name,
	int totalQuantity,
	int remainingQuantity,
	int issuedQuantity,
	long usedQuantity,
	LocalDateTime expirationAt
) {
}
