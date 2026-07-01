package com.example.thetunais4joteamproject.domain.product.dto;

import java.util.List;

import com.example.thetunais4joteamproject.domain.product.entity.OptionStatus;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;

public record GetProductDetailResponse(
        Long id,
        String name,
        int price,
        String description,
        ProductStatus status,
        String categoryName,
        String imageUrl,
        List<ProductOptionResponse> options
) {
    // 상품 엔티티와 변환된 옵션 DTO 리스트를 받아 결합하는 정적 팩토리 메서드
    public static GetProductDetailResponse of(Product product, List<ProductOptionResponse> options) {
        return new GetProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStatus(),
                product.getCategory().getName(),
                product.getImageUrl(),
                options
        );
    }

    /**
     * 상품별 세부 옵션 정보를 담는 내부 응답 레코드
     */
    public record ProductOptionResponse(
            Long optionId,
            String optionName,
            int optionStock,
            int additionalPrice,
            OptionStatus status
    ) {
        // 옵션 엔티티를 DTO로 변환하는 정적 팩토리 메서드
        public static ProductOptionResponse from(ProductOption productOption) {
            return new ProductOptionResponse(
                    productOption.getId(),
                    productOption.getOptionName(),
                    productOption.getOptionStock(),
                    productOption.getAdditionalPrice(),
                    productOption.getStatus()
            );
        }
    }
}