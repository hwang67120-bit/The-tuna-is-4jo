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

    // 빌더 패턴을 통한 안전한 객체 생성
    public Category(String name) {
        this.name = name;
    }
}
