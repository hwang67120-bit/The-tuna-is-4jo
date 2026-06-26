package com.example.thetunais4joteamproject.domain.coupon.dto;

import jakarta.validation.constraints.NotNull;

public record RestoreCouponRequest(
        @NotNull(message = "복구할 회원 쿠폰 ID는 필수 항목입니다.")
        Long memberCouponId
) {
}
