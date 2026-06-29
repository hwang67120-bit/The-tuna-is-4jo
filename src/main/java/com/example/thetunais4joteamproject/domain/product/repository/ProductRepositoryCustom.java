package com.example.thetunais4joteamproject.domain.product.repository;

import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse.SearchProductItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<SearchProductItem> searchProductsByKeyword(String keyword, Pageable pageable);
}
