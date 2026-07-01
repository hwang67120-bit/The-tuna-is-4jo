package com.example.thetunais4joteamproject.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @NotNull(message = "상품 카테고리는 필수 입력 값입니다.")
        Long categoryId,

        @NotBlank(message = "상품 이름은 필수 입력 값이며 공백일 수 없습니다.")
        @Size(max = 255, message = "상품 이름은 최대 255자까지 가능합니다.")
        String name,

        @PositiveOrZero(message = "상품 가격은 0원 이상이어야 합니다.")
        int price,

        @Size(max = 5000, message = "상품 설명은 최대 5000자까지 입력 가능합니다.")
        String description,

        @Size(max = 1000, message = "이미지 URL은 최대 1000자까지 가능합니다.")
        String imageUrl
) {

    public static UpdateProductRequest of(Long categoryId, String name, int price, String description, String imageUrl) {
        return new UpdateProductRequest(categoryId, name, price, description, imageUrl);
    }
}
