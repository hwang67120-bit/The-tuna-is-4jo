package com.example.thetunais4joteamproject.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.thetunais4joteamproject.global.common.BaseEntity;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Builder
    public Product(Category category, String name, int price, String description, ProductStatus status) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.status = status;
    }

    // 비즈니스 로직: 상품 마스터 정보 수정 메서드
    public void updateProduct(Category category, String name, int price, String description) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    // 비즈니스 로직: 상품 상태 변경
    public void changeStatus(ProductStatus status) {
        this.status = status;
    }
}
