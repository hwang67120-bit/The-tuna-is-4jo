package com.example.thetunais4joteamproject.domain.coupon.dto;

import com.example.thetunais4joteamproject.domain.coupon.entity.MemberCouponStatus;
import java.time.LocalDateTime;

public record MemberCouponInfoResponse(
        Long memberCouponId,
        String name,
        int discountPrice,
        int minOrderPrice,
        LocalDateTime expirationAt,
        MemberCouponStatus couponStatus
) {
}
