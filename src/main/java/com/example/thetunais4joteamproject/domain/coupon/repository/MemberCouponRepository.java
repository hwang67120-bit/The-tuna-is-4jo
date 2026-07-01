package com.example.thetunais4joteamproject.domain.coupon.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.thetunais4joteamproject.domain.coupon.entity.MemberCoupon;

@Repository
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long>, MemberCouponRepositoryCustom {

    // 중복 발급 방지: 특정 회원이 특정 쿠폰을 이미 보유하고 있는지 확인.
    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);

    // 쿠폰 사용/차감 및 복구 시 레이스 컨디션을 막기 위해 조회 시점에 DB Row Lock을 획득
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mc from MemberCoupon mc where mc.id = :id")
    Optional<MemberCoupon> findByIdWithLock(@Param("id") Long id);

    /**
     * 유효기간이 지난 UNUSED 상태의 회원 쿠폰들을 일괄 EXPIRED로 스위칭
     */
    @Modifying(clearAutomatically = true) // 벌크 연산 후 영속성 컨텍스트를 깔끔하게 비워 정합성을 지킵니다.
    @Query("UPDATE MemberCoupon mc " +
           "SET mc.couponStatus = com.example.thetunais4joteamproject.domain.coupon.entity.MemberCouponStatus.EXPIRED " +
           "WHERE mc.couponStatus = com.example.thetunais4joteamproject.domain.coupon.entity.MemberCouponStatus.UNUSED " +
           "AND mc.coupon.expirationAt < :now")
    int updateExpiredCoupons(@Param("now") LocalDateTime now);
}
