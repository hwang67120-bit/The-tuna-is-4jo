package com.example.thetunais4joteamproject.domain.product.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.thetunais4joteamproject.domain.product.dto.CategoryResponse;
import com.example.thetunais4joteamproject.domain.product.dto.CreateCategoryRequest;
import com.example.thetunais4joteamproject.domain.product.entity.Category;
import com.example.thetunais4joteamproject.domain.product.repository.CategoryRepository;
import com.example.thetunais4joteamproject.domain.product.repository.ProductRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * 카테고리 목록 조회
     */
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(Category::getId))
            .map(CategoryResponse::from)
            .toList();
    }

    /**
     * 카테고리 생성 (Admin)
     */
    @Transactional
    public Long createCategory(CreateCategoryRequest request) {
        Category category = Category.from(request.name());
        categoryRepository.save(category);

        return category.getId();
    }

    /**
     * 카테고리 삭제 (Admin)
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.CATEGORY_NOT_FOUND));

        if (productRepository.existsByCategoryId(categoryId)) {
            throw BusinessException.from(ErrorCode.CONFLICT);
        }

        categoryRepository.delete(category);
    }
}
