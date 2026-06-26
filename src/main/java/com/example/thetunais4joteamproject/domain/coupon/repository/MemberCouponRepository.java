package com.example.thetunais4joteamproject.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.thetunais4joteamproject.domain.coupon.entity.MemberCoupon;

@Repository
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long>, MemberCouponRepositoryCustom {

    // 중복 발급 방지: 특정 회원이 특정 쿠폰을 이미 보유하고 있는지 확인.
    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);
}
