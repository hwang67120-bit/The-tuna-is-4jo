package com.example.thetunais4joteamproject.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
	@NotBlank(message = "카테고리 이름은 필수 입력 값입니다.")
	@Size(max = 50, message = "카테고리 이름은 최대 50자까지 가능합니다.")
	String name
) {
	public static CreateCategoryRequest from(String name) {
		return new CreateCategoryRequest(name);
	}
}
