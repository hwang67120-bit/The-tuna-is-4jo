package com.example.thetunais4joteamproject.domain.coupon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateCouponRequest(
        @NotBlank(message = "쿠폰 이름은 필수 항목입니다.")
        String name,

        @Min(value = 1, message = "할인 금액은 최소 1원 이상이어야 합니다.")
        int discountPrice,

        @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
        int minOrderPrice,

        @Min(value = 1, message = "발급 수량은 최소 1개 이상이어야 합니다.")
        int totalQuantity,

        @NotNull(message = "만료 일시는 필수 항목입니다.")
        LocalDateTime expirationAt
) {
}