package com.example.thetunais4joteamproject.domain.coupon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UseCouponRequest(
        @NotNull(message = "사용할 회원 쿠폰 ID는 필수 항목입니다.")
        Long memberCouponId,

        @Min(value = 0, message = "주문 금액은 0원 이상이어야 합니다.")
        int orderPrice
) {
}
