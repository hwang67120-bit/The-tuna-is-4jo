package com.example.thetunais4joteamproject.domain.product.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateProductResponse {

	private Long productId;

	// 정적 팩토리 메서드 사용을 위해 생성자는 private으로 제한
	private CreateProductResponse(Long productId) {
		this.productId = productId;
	}

	// 정적 팩토리 메서드 공통 규칙을 반영한 생성 메서드
	public static CreateProductResponse from(Long productId) {
		return new CreateProductResponse(productId);
	}
}
