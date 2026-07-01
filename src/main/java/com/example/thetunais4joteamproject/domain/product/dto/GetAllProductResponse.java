package com.example.thetunais4joteamproject.domain.product.dto;

import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;

public record GetAllProductResponse(
        Long id,
        String name,
        int price,
        String description,
        ProductStatus status,
        String categoryName,
        String imageUrl
) {
    // 엔티티 객체를 받아 DTO로 안전하게 전환해 주는 정적 팩토리 메서드
    public static GetAllProductResponse from(Product product) {
        return new GetAllProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStatus(),
                product.getCategory().getName(),
                product.getImageUrl()
        );
    }
}