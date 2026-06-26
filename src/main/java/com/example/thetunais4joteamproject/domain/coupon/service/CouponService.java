package com.example.thetunais4joteamproject.domain.coupon.service;

import com.example.thetunais4joteamproject.domain.coupon.dto.CreateCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.entity.Coupon;
import com.example.thetunais4joteamproject.domain.coupon.repository.CouponRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    /**
     * [Admin] 신규 쿠폰 정책 생성
     */
    @Transactional
    public Long createCoupon(CreateCouponRequest request) {
        // 비즈니스 방어선: 쿠폰 만료일이 현재 시간보다 과거라면 예외를 터트립니다.
        if (request.expirationAt().isBefore(LocalDateTime.now())) {
            throw BusinessException.from(ErrorCode.INVALID_COUPON_EXPIRATION);
        }

        // 정적 팩토리 메서드를 통해 엔티티 객체를 조립합니다.
        Coupon coupon = Coupon.of(
                request.name(),
                request.discountPrice(),
                request.minOrderPrice(),
                request.totalQuantity(),
                request.expirationAt()
        );

        // 데이터베이스에 최종 영속화합니다.
        couponRepository.save(coupon);

        // 생성된 쿠폰 원판의 식별자(ID)를 반환합니다.
        return coupon.getId();
    }
}