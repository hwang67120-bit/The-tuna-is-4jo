package com.example.thetunais4joteamproject.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    // 특정 상품의 기본/대표 옵션 데이터를 조회하는 쿼리메서드
    Optional<ProductOption> findTopByProductIdOrderByIdAsc(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ProductOption o where o.id = :optionId")
    Optional<ProductOption> findByIdWithLock(@Param("optionId") Long optionId);

    List<ProductOption> findAllByProductId(Long productId);
}
