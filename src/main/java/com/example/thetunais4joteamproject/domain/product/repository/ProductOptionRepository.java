package com.example.thetunais4joteamproject.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

	// 특정 상품 하위의 모든 옵션 리스트 조회 (상세 조회 시 활용)
    List<ProductOption> findByProductId(Long productId);

    // 특정 상품의 기본/대표 옵션 데이터를 조회하는 쿼리메서드
    Optional<ProductOption> findTopByProductIdOrderByIdAsc(Long productId);
}
