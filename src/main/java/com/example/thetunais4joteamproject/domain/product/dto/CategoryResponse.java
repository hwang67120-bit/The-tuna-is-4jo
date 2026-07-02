package com.example.thetunais4joteamproject.domain.product.dto;

import com.example.thetunais4joteamproject.domain.product.entity.Category;

public record CategoryResponse(
	Long categoryId,
	String name
) {

	public static CategoryResponse from(Category category) {
		return new CategoryResponse(
			category.getId(),
			category.getName()
		);
	}
}
