package com.example.thetunais4joteamproject.domain.product.dto;

import java.util.List;

import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;

public record SearchProductResponse(
        List<SearchProductItem> products,
        int page,
        int size,
        long totalElements
) {
    public static SearchProductResponse of(List<SearchProductItem> products, int page, int size, long totalElements) {
        return new SearchProductResponse(products, page, size, totalElements);
    }

    public record SearchProductItem(
            Long productId,
            String productName,
            int price,
            int stock,
            ProductStatus saleStatus,
            String imageUrl
    ) {
    }
}
