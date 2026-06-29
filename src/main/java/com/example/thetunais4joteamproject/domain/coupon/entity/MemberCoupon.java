package com.example.thetunais4joteamproject.domain.coupon.entity;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberCouponStatus couponStatus;

    private LocalDateTime usedAt;

    private MemberCoupon(Long memberId, Coupon coupon) {
        this.memberId = memberId;
        this.coupon = coupon;
        this.couponStatus = MemberCouponStatus.UNUSED;
    }

    public static MemberCoupon of(Long memberId, Coupon coupon) {
        return new MemberCoupon(memberId, coupon);
    }

    // 비즈니스 메서드: 쿠폰 사용 확정
    public void use(int orderPrice) {
        validateUsable(orderPrice);

        this.couponStatus = MemberCouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public int calculateDiscountPrice(int orderPrice) {
        validateUsable(orderPrice);

        return this.coupon.getDiscountPrice();
    }

    private void validateUsable(int orderPrice) {
        if (this.couponStatus != MemberCouponStatus.UNUSED) {
            throw new IllegalArgumentException("이미 사용되었거나 만료된 쿠폰입니다.");
        }
        if (orderPrice < this.coupon.getMinOrderPrice()) {
            throw new IllegalArgumentException("주문 금액이 쿠폰의 최소 주문 금액보다 적습니다.");
        }
        if (this.coupon.getDiscountPrice() > orderPrice) {
            throw new IllegalArgumentException("쿠폰 할인 금액이 주문 금액보다 클 수 없습니다.");
        }
        if (LocalDateTime.now().isAfter(this.coupon.getExpirationAt())) {
            this.couponStatus = MemberCouponStatus.EXPIRED;
            throw new IllegalArgumentException("만료된 쿠폰입니다.");
        }
    }

    // 비즈니스 메서드: 주문 취소 시 복구
    public void restore() {
        if (this.couponStatus != MemberCouponStatus.USED) {
            throw new IllegalArgumentException("사용 완료 상태의 쿠폰만 복구할 수 있습니다.");
        }
        if (LocalDateTime.now().isAfter(this.coupon.getExpirationAt())) {
            this.couponStatus = MemberCouponStatus.EXPIRED;
            return;
        }
        this.couponStatus = MemberCouponStatus.UNUSED;
        this.usedAt = null;
    }
}
