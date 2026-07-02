package com.example.thetunais4joteamproject.domain.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import com.example.thetunais4joteamproject.domain.product.dto.SearchPopularResponse;
import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse.SearchProductItem;
import com.example.thetunais4joteamproject.domain.product.entity.ProductStatus;
import com.example.thetunais4joteamproject.domain.product.repository.ProductRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

	@InjectMocks
	private ProductSearchService productSearchService;

	@Mock
	private ProductRepository productRepository;
	@Mock
	private StringRedisTemplate redisTemplate;
	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private ZSetOperations<String, String> zSetOperations;
	@Mock
	private ValueOperations<String, String> valueOperations;

	@Test
	@DisplayName("상품 검색 시 캐시 히트(Cache Hit)가 발생하면 DB 조회 없이 즉시 캐시된 데이터를 반환한다.")
	void searchProducts_CacheHit_ReturnsCachedData() throws Exception {
		// given
		String keyword = "tuna";
		Pageable pageable = PageRequest.of(0, 10);
		String cacheKey = "product:search:tuna:0:10";
		String cachedJson = "{\"products\":[],\"page\":0,\"size\":10,\"totalElements\":0}";
		SearchProductResponse mockResponse = SearchProductResponse.of(Collections.emptyList(), 0, 10, 0);

		given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(cacheKey)).willReturn(cachedJson);
		given(objectMapper.readValue(cachedJson, SearchProductResponse.class)).willReturn(mockResponse);

		// when
		SearchProductResponse response = productSearchService.searchProducts(keyword, pageable);

		// then
		assertThat(response).isNotNull();
		verify(zSetOperations, times(1)).incrementScore("popular_search", keyword, 1.0);
		verify(productRepository, never()).searchProductsByKeyword(anyString(), any(Pageable.class));
	}

	@Test
	@DisplayName("상품 검색 시 캐시 미스(Cache Miss)가 발생하면 DB를 조회하고 그 결과를 Redis에 캐싱한다.")
	void searchProducts_CacheMiss_QueriesDBAndCaches() throws Exception {
		// given
		String keyword = "tuna";
		Pageable pageable = PageRequest.of(0, 10);
		String cacheKey = "product:search:tuna:0:10";
		String serializedJson = "{\"products\":[],\"page\":0,\"size\":10,\"totalElements\":1}";

		SearchProductItem mockItem = new SearchProductItem(1L, "참치캔", 2000, 100, ProductStatus.ON_SALE, null);
		Page<SearchProductItem> mockPage = new PageImpl<>(List.of(mockItem), pageable, 1);
		SearchProductResponse mockResponse = SearchProductResponse.of(List.of(mockItem), 0, 10, 1);

		given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(cacheKey)).willReturn(null); // Cache Miss
		given(productRepository.searchProductsByKeyword(keyword, pageable)).willReturn(mockPage);
		given(objectMapper.writeValueAsString(any(SearchProductResponse.class))).willReturn(serializedJson);

		// when
		SearchProductResponse response = productSearchService.searchProducts(keyword, pageable);

		// then
		assertThat(response).isNotNull();
		assertThat(response.products()).hasSize(1);
		assertThat(response.products().get(0).productName()).isEqualTo("참치캔");

		verify(productRepository, times(1)).searchProductsByKeyword(keyword, pageable);
		verify(valueOperations, times(1)).set(cacheKey, serializedJson, 10L, TimeUnit.MINUTES);
	}

	@Test
	@DisplayName("상품 검색 시 결과가 없으면 빈 결과를 리턴한다.")
	void searchProducts_NoResult_ReturnsEmptyResponse() throws Exception {
		// given
		String keyword = "non_exist";
		Pageable pageable = PageRequest.of(0, 10);
		String cacheKey = "product:search:non_exist:0:10";
		Page<SearchProductItem> emptyPage = Page.empty(pageable);
		String serializedJson = "{\"products\":[],\"page\":0,\"size\":10,\"totalElements\":0}";

		given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(cacheKey)).willReturn(null);
		given(productRepository.searchProductsByKeyword(keyword, pageable)).willReturn(emptyPage);
		given(objectMapper.writeValueAsString(any(SearchProductResponse.class))).willReturn(serializedJson);

		// when
		SearchProductResponse response = productSearchService.searchProducts(keyword, pageable);

		// then
		assertThat(response).isNotNull();
		assertThat(response.products()).isEmpty();
		assertThat(response.totalElements()).isZero();
	}

	@Test
	@DisplayName("인기 검색어 조회 시 Redis Sorted Set을 기반으로 1위부터 10위까지 정상 조회한다.")
	void getPopularSearches_Success() {
		// given
		Set<ZSetOperations.TypedTuple<String>> rankedSet = new LinkedHashSet<>();
		rankedSet.add(new DefaultTypedTuple<>("참치", 10.0));
		rankedSet.add(new DefaultTypedTuple<>("김", 8.0));

		given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
		given(zSetOperations.reverseRangeWithScores("popular_search", 0, 9)).willReturn(rankedSet);

		// when
		SearchPopularResponse response = productSearchService.getPopularSearches();

		// then
		assertThat(response).isNotNull();
		assertThat(response.keywords()).hasSize(2);
		assertThat(response.keywords().get(0).rank()).isEqualTo(1);
		assertThat(response.keywords().get(0).keyword()).isEqualTo("참치");
		assertThat(response.keywords().get(0).score()).isEqualTo(10.0);
	}
}
