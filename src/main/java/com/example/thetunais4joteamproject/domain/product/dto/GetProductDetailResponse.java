package com.example.thetunais4joteamproject.domain.product.dto;

import com.example.thetunais4joteamproject.domain.product.entity.OptionStatus;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetProductDetailResponse {

    private Long id;
    private String name;
    private int price;
    private String description;
    private ProductStatus status;
    private String categoryName;
    private List<ProductOptionResponse> options;

    private GetProductDetailResponse(Long id, String name, int price, String description, ProductStatus status, String categoryName, List<ProductOptionResponse> options) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.status = status;
        this.categoryName = categoryName;
        this.options = options;
    }

    // // 상품 엔티티와 변환된 옵션 DTO 리스트를 받아 결합하는 정적 팩토리 메서드
    public static GetProductDetailResponse of(Product product, List<ProductOptionResponse> options) {
        return new GetProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStatus(),
                product.getCategory().getName(),
                options
        );
    }

    /**
     * 상품별 세부 옵션 정보를 담는 내부 응답 DTO
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ProductOptionResponse {
        private Long optionId;
        private String optionName;
        private int optionStock;
        private int additionalPrice;
        private OptionStatus status;

        private ProductOptionResponse(Long optionId, String optionName, int optionStock, int additionalPrice, OptionStatus status) {
            this.optionId = optionId;
            this.optionName = optionName;
            this.optionStock = optionStock;
            this.additionalPrice = additionalPrice;
            this.status = status;
        }

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
