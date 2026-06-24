package com.example.thetunais4joteamproject.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateProductRequest {
	
	@NotNull(message = "카테고리 ID는 필수 입력 값입니다.")
    private Long categoryId;

    @NotBlank(message = "상품 이름은 필수 입력 값이며 공백일 수 없습니다.")
    @Size(max = 255, message = "상품 이름은 최대 255자까지 가능합니다.")
    private String name;

    @PositiveOrZero(message = "상품 가격은 0원 이상이어야 합니다.")
    private int price;

    @Size(max = 5000, message = "상품 설명은 최대 5000자까지 입력 가능합니다.")
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
