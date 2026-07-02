package com.example.thetunais4joteamproject.domain.product.service;

import com.example.thetunais4joteamproject.domain.product.dto.SearchPopularResponse;
import com.example.thetunais4joteamproject.domain.product.dto.SearchPopularResponse.SearchPopularItem;
import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse.SearchProductItem;
import com.example.thetunais4joteamproject.domain.product.repository.ProductRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService {

	private static final String POPULAR_SEARCH_KEY = "popular_search";
	private static final String CACHE_PREFIX = "product:search:";
	private static final long CACHE_TTL_MINUTES = 10L;

	private final ProductRepository productRepository;
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * 시나리오 1. 실시간 검색어 가중치 가산 및 고속 분산 검색 캐싱
	 */
	@Transactional
	public SearchProductResponse searchProducts(String keyword, Pageable pageable) {
		redisTemplate.opsForZSet().incrementScore(POPULAR_SEARCH_KEY, keyword, 1.0);

		String cacheKey = CACHE_PREFIX + keyword + ":" + pageable.getPageNumber() + ":" + pageable.getPageSize();

		String cachedJson = redisTemplate.opsForValue().get(cacheKey);
		if (cachedJson != null) {
			try {
				return objectMapper.readValue(cachedJson, SearchProductResponse.class);
			} catch (Exception exception) {
				// 직렬화 예외 방어선
			}
		}

		Page<SearchProductItem> searchPage = productRepository.searchProductsByKeyword(keyword, pageable);

		SearchProductResponse response = SearchProductResponse.of(
			searchPage.getContent(),
			searchPage.getNumber(),
			searchPage.getSize(),
			searchPage.getTotalElements()
		);

		try {
			String jsonToCache = objectMapper.writeValueAsString(response);
			redisTemplate.opsForValue().set(cacheKey, jsonToCache, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
		} catch (Exception exception) {
			// 캐시 적재 예외 가림막
		}

		return response;
	}

	/**
	 * 시나리오 2. 실시간 인기 검색어 상위 TOP 10 조회
	 */
	public SearchPopularResponse getPopularSearches() {
		Set<ZSetOperations.TypedTuple<String>> rankedSet = redisTemplate.opsForZSet()
			.reverseRangeWithScores(POPULAR_SEARCH_KEY, 0, 9);

		if (rankedSet == null) {
			throw BusinessException.from(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		List<SearchPopularItem> keywords = new ArrayList<>();
		int currentRank = 1;

		for (ZSetOperations.TypedTuple<String> tuple : rankedSet) {
			SearchPopularItem item = new SearchPopularItem(
				currentRank,
				tuple.getValue(),
				tuple.getScore()
			);
			keywords.add(item);
			currentRank++;
		}

		return SearchPopularResponse.of(keywords, LocalDateTime.now());
	}

	/**
	 * 상품 정보 변경(수정/삭제) 시 오염된 검색 캐시 데이터 일괄 무효화
	 */
	public void evictSearchCache() {
		Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}
}
