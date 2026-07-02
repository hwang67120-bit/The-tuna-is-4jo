package com.example.thetunais4joteamproject.domain.product.repository;

import static com.example.thetunais4joteamproject.domain.product.entity.QProduct.*;
import static com.example.thetunais4joteamproject.domain.product.entity.QProductOption.*;

import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse.SearchProductItem;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<SearchProductItem> searchProductsByKeyword(String keyword, Pageable pageable) {
		List<SearchProductItem> content = queryFactory
			.select(Projections.constructor(SearchProductItem.class,
				product.id,
				product.name,
				product.price,
				productOption.optionStock.sum().coalesce(0),
				product.status,
				product.imageUrl
			))
			.from(product)
			.leftJoin(productOption).on(productOption.product.eq(product))
			.where(
				product.status.eq(ProductStatus.ON_SALE),
				product.name.contains(keyword)
			)
			.groupBy(product.id)
			.orderBy(product.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(product.count())
			.from(product)
			.where(
				product.status.eq(ProductStatus.ON_SALE),
				product.name.contains(keyword)
			)
			.fetchOne();

		long totalElements = total != null ? total : 0L;

		return new PageImpl<>(content, pageable, totalElements);
	}

	@Override
	public List<Product> findAllByNoOffset(Long lastProductId, int size) {
		return queryFactory
			.selectFrom(product)
			.where(
				product.status.eq(ProductStatus.ON_SALE), // 판매 중인 상품만 필터링
				ltProductId(lastProductId) // 이전 마지막 ID보다 작은 구역으로 Seek 연산 수행
			)
			.orderBy(product.id.desc()) // 최신순 정렬 (ID 식별자 기준 고속 내림차순)
			.limit(size)
			.fetch();
	}

	/**
	 * No-Offset의 핵심 조건절 가드레일
	 * 첫 페이지 진입 시 lastProductId가 null로 유입되며, 이때는 해당 조건문이 무시되어 최신 데이터부터 로드된다.
	 */
	private BooleanExpression ltProductId(Long lastProductId) {
		if (lastProductId == null) {
			return null;
		}
		return product.id.lt(lastProductId);
	}
}
