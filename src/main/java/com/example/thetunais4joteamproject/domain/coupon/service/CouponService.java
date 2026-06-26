package com.example.thetunais4joteamproject.domain.coupon.service;

import com.example.thetunais4joteamproject.domain.coupon.dto.CouponAdminResponse;
import com.example.thetunais4joteamproject.domain.coupon.dto.CreateCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.IssueCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.MemberCouponInfoResponse;
import com.example.thetunais4joteamproject.domain.coupon.dto.RestoreCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.UseCouponRequest;
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

    /**
     * [주문 연동] 결제 진행 시 쿠폰 사용 처리
     */
    @Transactional
    public void useCoupon(Long memberId, UseCouponRequest request) {
        // 1. 회원의 쿠폰함에 해당 쿠폰이 존재하는지 조회 (원판 Coupon 정보도 Lazy로 필요할 때 조인되도록 findById 활용)
        MemberCoupon memberCoupon = memberCouponRepository.findById(request.memberCouponId())
                .orElseThrow(() -> {
                    return BusinessException.from(ErrorCode.COUPON_NOT_FOUND);
                });

        // 2. 소유권 방어선: 해당 쿠폰이 요청을 보낸 로그인 회원의 쿠폰이 맞는지 정밀 검증
        if (!memberCoupon.getMemberId().equals(memberId)) {
            throw BusinessException.from(ErrorCode.UNAUTHORIZED);
            // 💡 전역 ErrorCode에 권한 거부용 코드가 있다면 매핑해 주세요.
        }

        // 3. 엔티티 내부 핵심 비즈니스 메서드 호출
        try {
            memberCoupon.use(request.orderPrice());
        } catch (IllegalArgumentException e) {
            // 엔티티 내부에서 터진 예외 메시지에 따라 적절한 비즈니스 예외로 전환하여 프론트에 전달
            if (e.getMessage().contains("최소 주문 금액")) {
                throw BusinessException.from(ErrorCode.INVALID_COUPON_ORDER_PRICE);
            }
            throw BusinessException.from(ErrorCode.COUPON_EXPIRED);
        }
    }

    /**
     * [주문 연동] 주문 취소 시 쿠폰 복구 처리
     */
    @Transactional
    public void restoreCoupon(Long memberId, RestoreCouponRequest request) {
        // 1. 회원의 쿠폰함 존재 여부 검증
        MemberCoupon memberCoupon = memberCouponRepository.findById(request.memberCouponId())
                .orElseThrow(() -> {
                    return BusinessException.from(ErrorCode.COUPON_NOT_FOUND);
                });

        // 2. 소유권 방어선: 타인의 쿠폰 복구 요청 원천 차단
        if (!memberCoupon.getMemberId().equals(memberId)) {
            throw BusinessException.from(ErrorCode.UNAUTHORIZED);
        }

        // 3. 엔티티 내부 핵심 비즈니스 메서드 호출
        try {
            memberCoupon.restore();
        } catch (IllegalArgumentException e) {
            throw BusinessException.from(ErrorCode.COUPON_NOT_USED);
        }
    }

    /**
     * [관리자] 전역 쿠폰 발급 및 사용 현황 통계 조회
     */
    public List<CouponAdminResponse> getCouponsManagementStatus() {
        return couponRepository.findCouponsManagementStatus();
    }
}