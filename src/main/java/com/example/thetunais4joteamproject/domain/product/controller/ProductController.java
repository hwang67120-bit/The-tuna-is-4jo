package com.example.thetunais4joteamproject.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.thetunais4joteamproject.domain.product.dto.CreateProductRequest;
import com.example.thetunais4joteamproject.domain.product.dto.RepresentStockRequest;
import com.example.thetunais4joteamproject.domain.product.dto.UpdateOptionRequest;
import com.example.thetunais4joteamproject.domain.product.service.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 생성
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody CreateProductRequest request) {
        Long productId = productService.createProduct(request);

        // 프로젝트 표준 성공 응답 규격 포맷팅
        Map<String, Object> response = new HashMap<>();
        response.put("status", 201);
        response.put("message", "상품 생성 성공");
        response.put("data", Map.of("productId", productId));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 상품 세부 옵션, 상태, 추가금액 일괄 변경
     */
    @PutMapping("/{productId}/options")
    public ResponseEntity<Map<String, Object>> updateOptionStocks(
        @PathVariable Long productId,
        @RequestBody Map<String, List<UpdateOptionRequest>> requestBody) {

        List<UpdateOptionRequest> requests = requestBody.get("options");
        productService.updateOptionStocks(productId, requests);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "상품 옵션 재고 및 상태 변경 성공");
        response.put("data", null);

        return ResponseEntity.ok(response);
    }

    /**
     * 상품 대표 재고 변경
     */
    @PutMapping("/{productId}/stock")
    public ResponseEntity<Map<String, Object>> updateRepresentativeStock(
        @PathVariable Long productId,
        @RequestBody RepresentStockRequest request) {

        productService.updateRepresentativeStock(productId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "상품 대표 재고 수량 설정 성공");
        response.put("data", null);

        return ResponseEntity.ok(response);
    }
}