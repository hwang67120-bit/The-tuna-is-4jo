package com.example.thetunais4joteamproject.domain.coupon.dto;

import jakarta.validation.constraints.NotNull;

public record IssueCouponRequest(
        @NotNull(message = "발급할 쿠폰 ID는 필수 항목입니다.")
        Long couponId
) {
}
