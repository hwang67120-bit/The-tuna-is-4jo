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

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    // 정적 팩토리 메서드 사용을 강제하기 위해 외부 생성자를 private으로 차단
    private Product(Long memberId, Category category, String name, int price, String description, String imageUrl, ProductStatus status) {
        this.memberId = memberId;
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    // 정적 팩토리 메서드
    public static Product of(Long memberId, Category category, String name, int price, String description, ProductStatus status) {
        return Product.of(memberId, category, name, price, description, null, status);
    }

    // 이미지 URL을 포함하는 신규 정적 팩토리 메서드
    public static Product of(Long memberId, Category category, String name, int price, String description, String imageUrl, ProductStatus status) {
        Product product = new Product();
        product.memberId = memberId;
        product.category = category;
        product.name = name;
        product.price = price;
        product.description = description;
        product.imageUrl = imageUrl;
        product.status = status;
        return product;
    }

    public void updateProduct(Category category, String name, int price, String description, String imageUrl) {
        // 카테고리 참조 변경 무결성 보장
        if (category != null) {
            this.category = category;
        }
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void changeStatus(ProductStatus status) {
        if (status != null) {
            this.status = status;
        }
    }
}
