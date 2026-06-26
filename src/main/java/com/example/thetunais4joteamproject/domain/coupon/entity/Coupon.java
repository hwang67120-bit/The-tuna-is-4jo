package com.example.thetunais4joteamproject.domain.coupon.entity;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int discountPrice;

    @Column(nullable = false)
    private int minOrderPrice;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int remainingQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus couponStatus;

    @Column(nullable = false)
    private LocalDateTime expirationAt;

    private Coupon(String name, int discountPrice, int minOrderPrice, int totalQuantity, LocalDateTime expirationAt) {
        this.name = name;
        this.discountPrice = discountPrice;
        this.minOrderPrice = minOrderPrice;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.couponStatus = CouponStatus.ACTIVE;
        this.expirationAt = expirationAt;
    }

    public static Coupon of(String name, int discountPrice, int minOrderPrice, int totalQuantity, LocalDateTime expirationAt) {
        return new Coupon(name, discountPrice, minOrderPrice, totalQuantity, expirationAt);
    }

    // 비즈니스 메서드: 발급 수량 차감
    public void decreaseRemainingQuantity() {
        if (this.remainingQuantity <= 0) {
            throw new IllegalArgumentException("발급 가능한 쿠폰 수량이 소진되었습니다.");
        }
        this.remainingQuantity--;
    }

    // 비즈니스 메서드: 관리자 상태 변경
    public void changeStatus(CouponStatus status) {
        this.couponStatus = status;
    }
}