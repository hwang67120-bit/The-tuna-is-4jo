package com.example.thetunais4joteamproject.domain.coupon.repository;

import com.example.thetunais4joteamproject.domain.coupon.dto.MemberCouponInfoResponse;
import com.example.thetunais4joteamproject.domain.coupon.entity.QCoupon;
import com.example.thetunais4joteamproject.domain.coupon.entity.QMemberCoupon;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MemberCouponRepositoryImpl implements MemberCouponRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<MemberCouponInfoResponse> findMyCoupons(Long memberId) {
		QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;
		QCoupon coupon = QCoupon.coupon;

		return queryFactory
			.select(Projections.constructor(MemberCouponInfoResponse.class,
				memberCoupon.id,
				coupon.name,
				coupon.discountPrice,
				coupon.minOrderPrice,
				coupon.expirationAt,
				memberCoupon.couponStatus
			))
			.from(memberCoupon)
			.join(memberCoupon.coupon, coupon) // 묵시적 조인이 아닌 명시적 Inner Join
			.where(memberCoupon.memberId.eq(memberId))
			.fetch();
	}
}
