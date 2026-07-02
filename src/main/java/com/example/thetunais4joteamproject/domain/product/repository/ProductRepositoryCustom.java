package com.example.thetunais4joteamproject.domain.product.repository;

import java.util.List;

import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse.SearchProductItem;
import com.example.thetunais4joteamproject.domain.product.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
	Page<SearchProductItem> searchProductsByKeyword(String keyword, Pageable pageable);

	/**
	 * No-Offset 기반 고속 상품 목록 조회
	 * @param lastProductId 이전 페이지의 가장 마지막 상품 ID (첫 페이지 조회 시 null)
	 * @param size 가져올 페이지 크기
	 */
	List<Product> findAllByNoOffset(Long lastProductId, int size);
}
