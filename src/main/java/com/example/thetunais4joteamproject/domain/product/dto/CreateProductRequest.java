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
}
