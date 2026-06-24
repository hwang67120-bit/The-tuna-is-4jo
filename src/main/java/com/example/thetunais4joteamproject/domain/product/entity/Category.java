package com.example.thetunais4joteamproject.domain.product.entity;

import com.example.thetunais4joteamproject.global.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 무분별한 객체 생성 방지
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50)
    private String name;

    // 정적 팩토리 메서드 사용을 강제하기 위해 외부 생성자를 private으로 통제.
    private Category(String name) {
        this.name = name;
    }

    // 단일 인자를 받아 인스턴스를 반환하는 정적 팩토리 생성 메서드.
    public static Category from(String name) {
        return new Category(name);
    }
}
