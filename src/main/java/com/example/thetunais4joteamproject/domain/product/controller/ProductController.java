package com.example.thetunais4joteamproject.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.thetunais4joteamproject.domain.product.dto.CreateProductRequest;
import com.example.thetunais4joteamproject.domain.product.dto.CreateProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetAllProductResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetCategoryProductsResponse;
import com.example.thetunais4joteamproject.domain.product.dto.GetProductDetailResponse;
import com.example.thetunais4joteamproject.domain.product.dto.RepresentStockRequest;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateOptionRequest;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateProductRequest;
import com.example.thetunais4joteamproject.domain.product.service.ProductService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

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

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(createProductResponse));
    }

    /**
     * 상품 세부 옵션, 상태, 추가금액 일괄 변경
     */
    @PutMapping("/{productId}/options")
    public ResponseEntity<ApiResponse<Void>> updateProductOptions(
		@AuthenticationPrincipal
        Long memberId,
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
    public ResponseEntity<ApiResponse<Void>> updateProductStock(
		@AuthenticationPrincipal
        Long memberId,
        @PathVariable
        Long productId,
        @RequestBody
        RepresentStockRequest representStockRequest
    ) {
        productService.updateRepresentativeStock(productId, representStockRequest);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(null));
    }

    /**
     * 상품 목록 조회 (시나리오 반영)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetAllProductResponse>>> getAllProducts(
        @PageableDefault
        Pageable pageable
    ) {
        Page<GetAllProductResponse> getAllProductResponse = productService.getAllProducts(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(getAllProductResponse));
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
    public ResponseEntity<ApiResponse<Void>> updateProduct(
		@AuthenticationPrincipal
        Long memberId,
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
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
        @AuthenticationPrincipal
        Long memberId,
		@PathVariable
        Long productId
    ) {
        productService.deleteProduct(productId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(null));
    }
}
