package com.example.thetunais4joteamproject.domain.product.controller;

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
import com.example.thetunais4joteamproject.domain.product.dto.RepresentStockRequest;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateOptionRequest;
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
        CustomUserDetails customUserDetails,
        @RequestBody
        CreateProductRequest createProductRequest
    ) {
        // 인증 객체 내부에서 로그인한 관리자의 ID를 안전하게 적출
        Long memberId = customUserDetails.getId();

        Long productId = productService.createProduct(memberId, createProductRequest);

        CreateProductResponse createProductResponse = CreateProductResponse.from(productId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(createProductResponse));
    }

    /**
     * 상품 세부 옵션, 상태, 추가금액 일괄 변경
     */
    @PutMapping("/{productId}/options")
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
     * 상품 목록 조회 (시나리오 반영)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetAllProductResponse>>> getAllProducts(
            @PageableDefault(size = 10)
			Pageable pageable
    ) {
        Page<GetAllProductResponse> getAllProductResponse = productService.getAllProducts(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(getAllProductResponse));
    }
}