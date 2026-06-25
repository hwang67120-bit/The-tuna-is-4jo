package com.example.thetunais4joteamproject.domain.product.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.thetunais4joteamproject.domain.product.dto.CreateCategoryRequest;
import com.example.thetunais4joteamproject.domain.product.service.CategoryService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 생성 (Admin 전용)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createCategory(
        @Valid
        @RequestBody
		CreateCategoryRequest createCategoryRequest
    ) {
        Long categoryId = categoryService.createCategory(createCategoryRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(categoryId));
    }
}