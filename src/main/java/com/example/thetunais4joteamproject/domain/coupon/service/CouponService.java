package com.example.thetunais4joteamproject.domain.coupon.service;

import com.example.thetunais4joteamproject.domain.coupon.dto.CreateCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.IssueCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.MemberCouponInfoResponse;
import com.example.thetunais4joteamproject.domain.coupon.entity.Coupon;
import com.example.thetunais4joteamproject.domain.coupon.entity.MemberCoupon;
import com.example.thetunais4joteamproject.domain.coupon.repository.CouponRepository;
import com.example.thetunais4joteamproject.domain.coupon.repository.MemberCouponRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;

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

    /**
     * [사용자] 선착순 쿠폰 발급
     */
    @Transactional
    public Long issueCoupon(Long memberId, IssueCouponRequest request) {
        // 1. 쿠폰 원판 정책 존재 여부 확인
        Coupon coupon = couponRepository.findById(request.couponId())
                .orElseThrow(() -> {
                    return BusinessException.from(ErrorCode.COUPON_NOT_FOUND);
                });

        // 2. 쿠폰 유효기간 만료 여부 확인
        if (LocalDateTime.now().isAfter(coupon.getExpirationAt())) {
            throw BusinessException.from(ErrorCode.COUPON_EXPIRED);
        }

        // 3. 중복 발급 여부 확인 (유저당 딱 1번만 발급 허용)
        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, coupon.getId())) {
            throw BusinessException.from(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 4. 쿠폰 잔여 수량 확인 및 차감 (0개 이하면 내부에서 예외 발생)
        try {
            coupon.decreaseRemainingQuantity();
        } catch (IllegalArgumentException e) {
            throw BusinessException.from(ErrorCode.COUPON_OUT_OF_STOCK);
        }

        // 5. 회원의 쿠폰함에 신규 매핑 데이터 적재
        MemberCoupon memberCoupon = MemberCoupon.of(memberId, coupon);
        memberCouponRepository.save(memberCoupon);

        // 6. 생성된 회원 쿠폰 ID 반환
        return memberCoupon.getId();
    }

    /**
     * [사용자] 본인이 보유한 쿠폰 목록 전체 조회
     */
    public List<MemberCouponInfoResponse> getMyCoupons(Long memberId) {
        return memberCouponRepository.findMyCoupons(memberId);
    }
}