package com.example.thetunais4joteamproject.domain.product.dto;

import jakarta.validation.constraints.Min;

public record RepresentStockRequest(
	@Min(value = 0, message = "대표 재고 수량은 0개 이상이어야 합니다.")
	int stockQuantity
) {
}