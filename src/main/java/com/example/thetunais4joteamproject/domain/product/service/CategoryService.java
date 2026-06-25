package com.example.thetunais4joteamproject.domain.product.service;

import org.springframework.stereotype.Service;

import com.example.thetunais4joteamproject.domain.product.dto.CreateCategoryRequest;
import com.example.thetunais4joteamproject.domain.product.entity.Category;
import com.example.thetunais4joteamproject.domain.product.repository.CategoryRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 생성 (Admin)
     */
    @Transactional
    public Long createCategory(CreateCategoryRequest request) {
        Category category = Category.from(request.name());
        categoryRepository.save(category);

        return category.getId();
    }
}
