package com.example.thetunais4joteamproject.domain.product.service;

import com.example.thetunais4joteamproject.domain.product.dto.SearchPopularResponse;
import com.example.thetunais4joteamproject.domain.product.dto.SearchPopularResponse.SearchPopularItem;
import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse.SearchProductItem;
import com.example.thetunais4joteamproject.domain.product.repository.ProductRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

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
        // 1. Redis Sorted Set 점수 가산
        redisTemplate.opsForZSet().incrementScore(POPULAR_SEARCH_KEY, keyword, 1.0);

        // 2. 검색 조건별 고유 캐시 키 생성 (product:search:키워드:페이지번호:페이지크기)
        String cacheKey = CACHE_PREFIX + keyword + ":" + pageable.getPageNumber() + ":" + pageable.getPageSize();

        // 3. [v2 캐시 검색 단계] Cache Hit 점검
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, SearchProductResponse.class);
            } catch (Exception exception) {
                // 역직렬화 오류 시 안전하게 DB 조회를 유도하기 위해 빈 블록 유지
            }
        }

        // 4. [v1 DB 인덱스 검색 단계] Cache Miss 시 QueryDSL 복합 인덱스 조회
        Page<SearchProductItem> searchPage = productRepository.searchProductsByKeyword(keyword, pageable);

        // 실패 조건: 매칭되는 상품이 없을 경우 404 예외 규격 송출
        if (searchPage.isEmpty()) {
            throw BusinessException.from(ErrorCode.PRODUCT_NOT_FOUND);
        }

        SearchProductResponse response = SearchProductResponse.of(
                searchPage.getContent(),
                searchPage.getNumber(),
                searchPage.getSize(),
                searchPage.getTotalElements()
        );

        // 5. DB 조회 결과를 Redis 캐시 서버에 10분간 보관 (SETEX)
        try {
            String jsonToCache = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, jsonToCache, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception exception) {
            // 캐시 적재 예외가 코어 비즈니스를 셧다운시키지 않도록 예외 차단
        }

        return response;
    }

    /**
     * 시나리오 2. 실시간 인기 검색어 상위 TOP 10 조회
     */
    public SearchPopularResponse getPopularSearches() {
        // 1. 누적 점수가 높은 순으로 10개 키워드 추출
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
}