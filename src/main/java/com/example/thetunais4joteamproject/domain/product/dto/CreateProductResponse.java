package com.example.thetunais4joteamproject.domain.product.dto;

public record CreateProductResponse(
	Long productId
) {
	// 정적 팩토리 메서드 공통 규칙을 반영한 생성 메서드
	public static CreateProductResponse from(Long productId) {
		return new CreateProductResponse(productId);
	}
}