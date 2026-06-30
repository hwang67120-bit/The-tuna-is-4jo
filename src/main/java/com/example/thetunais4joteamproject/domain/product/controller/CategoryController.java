package com.example.thetunais4joteamproject.domain.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.thetunais4joteamproject.domain.product.dto.CategoryResponse;
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
     * 카테고리 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> responseData = categoryService.getCategories();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(responseData));
    }

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

    /**
     * 카테고리 삭제 (Admin 전용)
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
        @PathVariable
        Long categoryId
    ) {
        categoryService.deleteCategory(categoryId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("카테고리가 삭제되었습니다."));
    }
}
