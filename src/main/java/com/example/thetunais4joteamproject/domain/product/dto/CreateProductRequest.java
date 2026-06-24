package com.example.thetunais4joteamproject.domain.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateProductRequest {
	private Long categoryId;
	private String name;
	private int price;
	private String description;

	private CreateProductRequest(Long categoryId, String name, int price, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    // 정적 팩토리 메서드 공통 규칙을 반영
    public static CreateProductRequest of(Long categoryId, String name, int price, String description) {
        return new CreateProductRequest(categoryId, name, price, description);
    }
}
