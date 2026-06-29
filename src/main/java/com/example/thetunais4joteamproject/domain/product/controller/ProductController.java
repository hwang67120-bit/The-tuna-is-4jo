package com.example.thetunais4joteamproject.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.thetunais4joteamproject.domain.product.dto.CreateProductRequest;
import com.example.thetunais4joteamproject.domain.product.dto.CreateProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetAllProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetCategoryProductsResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetProductDetailResponse;
import com.example.thetunais4joteamproject.domain.product.dto.RepresentStockRequest;
import com.example.thetunais4joteamproject.domain.product.dto.SearchPopularResponse;
import com.example.thetunais4joteamproject.domain.product.dto.SearchProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateOptionRequest;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateProductRequest;
import com.example.thetunais4joteamproject.domain.product.service.ProductSearchService;
import com.example.thetunais4joteamproject.domain.product.service.ProductService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    /**
     * 상품 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateProductResponse>> create(
        @AuthenticationPrincipal
        Long memberId,
        @Valid
        @RequestBody
        CreateProductRequest createProductRequest
    ) {
        Long productId = productService.createProduct(memberId, createProductRequest);

        CreateProductResponse createProductResponse = CreateProductResponse.from(productId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(createProductResponse));
    }

    /**
     * 상품 세부 옵션, 상태, 추가금액 일괄 변경
     */
    @PutMapping("/{productId}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateProductOptions(
        @PathVariable
        Long productId,
        @RequestBody
        List<UpdateOptionRequest> updateOptionRequests
    ) {
        productService.updateOptionStocks(productId, updateOptionRequests);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(null));
    }

    /**
     * 상품 대표 재고 변경
     */
    @PutMapping("/{productId}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateProductStock(
        @PathVariable
        Long productId,
        @RequestBody
        RepresentStockRequest representStockRequest
    ) {
        productService.updateRepresentativeStock(productId, representStockRequest);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(null));
    }

    /**
     * 상품 목록 조회 (No-Offset 대용량 쿼리 최적화 반영)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetAllProductResponse>>> getAllProducts(
        @RequestParam(required = false) Long lastProductId,
        @RequestParam(defaultValue = "10") int size
    ) {
        List<GetAllProductResponse> responses = productService.getAllProductsNoOffset(lastProductId, size);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(responses));
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<GetProductDetailResponse>> getProductDetail(
        @PathVariable
        Long productId
    ) {
        GetProductDetailResponse getProductDetailResponse = productService.getProductDetail(productId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(getProductDetailResponse));
    }

    /**
     * 상품 카테고리 조회 (시나리오 반영)
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<GetCategoryProductsResponse>> getProductsByCategory(
            @PathVariable
            Long categoryId
    ) {
        GetCategoryProductsResponse getCategoryProductsResponse = productService.getProductsByCategory(categoryId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(getCategoryProductsResponse));
    }

    /**
     * 상품 수정
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
        @PathVariable
        Long productId,
        @Valid
        @RequestBody
		UpdateProductRequest updateProductRequest
    ) {
        productService.updateProduct(productId, updateProductRequest);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(null));
    }

    /**
     * 상품 삭제
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
		@PathVariable
        Long productId
    ) {
        productService.deleteProduct(productId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(null));
    }

    /**
     * 상품 통합 키워드 동적 검색 (Cache + QueryDSL 복합 인덱스)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchProductResponse>> searchProducts(
        @RequestParam(name = "keyword")
        String keyword,
        @PageableDefault
        Pageable pageable
    ) {
        SearchProductResponse response = productSearchService.searchProducts(keyword, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
    }

    /**
     * 실시간 인기 검색어 상위 TOP 10 순위 리스트 조회
     */
    @GetMapping("/popular-searches")
    public ResponseEntity<ApiResponse<SearchPopularResponse>> getPopularSearches() {
        SearchPopularResponse response = productSearchService.getPopularSearches();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
    }
}
