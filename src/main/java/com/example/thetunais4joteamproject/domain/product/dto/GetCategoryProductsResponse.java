package com.example.thetunais4joteamproject.domain.product.dto;

import java.util.List;

import com.example.thetunais4joteamproject.domain.product.entity.Category;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;

public record GetCategoryProductsResponse(
	Long categoryId,
	String categoryName,
	List<CategoryProductResponse> products
) {
	// // 카테고리 마스터 정보와 하위 상품 레코드 리스트를 정적 결합하는 팩토리 메서드
	public static GetCategoryProductsResponse of(Category category, List<CategoryProductResponse> products) {
		return new GetCategoryProductsResponse(
			category.getId(),
			category.getName(),
			products
		);
	}

	/**
	 * 카테고리에 속한 단건 상품 정보를 담는 내부 응답 레코드 (묵시적으로 static 처리됨)
	 */
	public record CategoryProductResponse(
		Long productId,
		String productName,
		int price,
		ProductStatus saleStatus,
		String imageUrl
	) {
		// // 상품 엔티티를 레코드 DTO로 전환하는 정적 팩토리 메서드
		public static CategoryProductResponse from(Product product) {
			return new CategoryProductResponse(
				product.getId(),
				product.getName(),
				product.getPrice(),
				product.getStatus(),
				product.getImageUrl()
			);
		}
	}
}
