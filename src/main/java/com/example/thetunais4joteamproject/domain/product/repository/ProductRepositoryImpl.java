package com.example.thetunais4joteamproject.domain.product.repository;

import static com.example.thetunais4joteamproject.domain.product.entity.QProduct.*;
import static com.example.thetunais4joteamproject.domain.product.entity.QProductOption.*;

import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse.SearchProductItem;
import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;
import com.querydsl.core.types.Projections;
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
                        product.status
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
}
