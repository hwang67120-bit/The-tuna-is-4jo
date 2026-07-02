package com.example.thetunais4joteamproject.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateCartItemRequest(
	@NotNull(message = "상품 옵션 ID는 필수입니다")
	Long productOptionId,

	@NotNull(message = "수량은 필수입니다")
	@Min(value = 1, message = "수량은 1개 이상이어야 합니다")
	Integer quantity
) {
}