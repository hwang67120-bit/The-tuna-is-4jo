package com.example.thetunais4joteamproject.domain.coupon.entity;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
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
            throw BusinessException.from(ErrorCode.COUPON_EXPIRED);
        }
        if (orderPrice < this.coupon.getMinOrderPrice()) {
            throw BusinessException.from(ErrorCode.INVALID_COUPON_ORDER_PRICE);
        }
        if (this.coupon.getDiscountPrice() > orderPrice) {
            throw BusinessException.from(ErrorCode.INVALID_COUPON_DISCOUNT_PRICE);
        }
        if (LocalDateTime.now().isAfter(this.coupon.getExpirationAt())) {
            this.couponStatus = MemberCouponStatus.EXPIRED;
            throw BusinessException.from(ErrorCode.COUPON_EXPIRED);
        }
    }

    // 비즈니스 메서드: 주문 취소 시 복구
    public void restore() {
        if (this.couponStatus != MemberCouponStatus.USED) {
            throw BusinessException.from(ErrorCode.COUPON_NOT_USED);
        }
        if (LocalDateTime.now().isAfter(this.coupon.getExpirationAt())) {
            this.couponStatus = MemberCouponStatus.EXPIRED;
            return;
        }
        this.couponStatus = MemberCouponStatus.UNUSED;
        this.usedAt = null;
    }
}
