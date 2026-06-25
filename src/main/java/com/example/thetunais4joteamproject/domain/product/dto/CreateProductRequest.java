package com.example.thetunais4joteamproject.domain.product.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotNull(message = "카테고리 ID는 필수 입력 값입니다.")
        Long categoryId,

        @NotBlank(message = "상품 이름은 필수 입력 값이며 공백일 수 없습니다.")
        @Size(max = 255, message = "상품 이름은 최대 255자까지 가능합니다.")
        String name,

        @PositiveOrZero(message = "상품 가격은 0원 이상이어야 합니다.")
        int price,

        @Size(max = 5000, message = "상품 설명은 최대 5000자까지 입력 가능합니다.")
        String description,

        @Valid
        @NotEmpty(message = "옵션 목록은 필수입니다. 빈 배열이라도 채워주세요.")
		List<ProductOptionRequest> options
) {
    // 정적 팩토리 메서드 공통 규칙 반영
    public static CreateProductRequest of(Long categoryId, String name, int price, String description, List<ProductOptionRequest> options) {
        return new CreateProductRequest(categoryId, name, price, description, options);
    }

    /**
     * 상품 생성 시 함께 등록할 옵션 정보 가방
     */
    public record ProductOptionRequest(
            @NotBlank(message = "옵션 이름은 필수 입력 값입니다.")
            String optionName,

            @Min(value = 0, message = "옵션 초기 재고는 0개 이상이어야 합니다.")
            int optionStock,

            @Min(value = 0, message = "옵션 추가 금액은 0원 이상이어야 합니다.")
            int additionalPrice
    ) {
    }
}