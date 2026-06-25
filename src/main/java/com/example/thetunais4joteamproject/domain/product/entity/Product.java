package com.example.thetunais4joteamproject.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.example.thetunais4joteamproject.global.common.BaseEntity;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    // N:1 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    // 정적 팩토리 메서드 사용을 강제하기 위해 외부 생성자를 private으로 차단
    private Product(Long memberId, Category category, String name, int price, String description, ProductStatus status) {
        this.memberId = memberId;
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.status = status;
    }

    // 정적 팩토리 메서드
    public static Product of(Long memberId, Category category, String name, int price, String description, ProductStatus status) {
        return new Product(memberId, category, name, price, description, status);
    }

    public void updateProduct(Category category, String name, int price, String description) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public void changeStatus(ProductStatus status) {
        this.status = status;
    }
}