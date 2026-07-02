package com.example.thetunais4joteamproject.domain.coupon.repository;

import com.example.thetunais4joteamproject.domain.coupon.dto.CouponAdminResponse;
import com.example.thetunais4joteamproject.domain.coupon.entity.MemberCouponStatus;
import com.example.thetunais4joteamproject.domain.coupon.entity.QCoupon;
import com.example.thetunais4joteamproject.domain.coupon.entity.QMemberCoupon;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<CouponAdminResponse> findCouponsManagementStatus() {
		QCoupon coupon = QCoupon.coupon;
		QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;

		return queryFactory
			.select(Projections.constructor(CouponAdminResponse.class,
				coupon.id,
				coupon.name,
				coupon.totalQuantity,
				coupon.remainingQuantity,
				coupon.totalQuantity.subtract(coupon.remainingQuantity), // 발급된 수량 계산 (총수량 - 잔여수량)
				ExpressionUtils.as(
					JPAExpressions.select(memberCoupon.count())
						.from(memberCoupon)
						.where(memberCoupon.coupon.id.eq(coupon.id)
							.and(memberCoupon.couponStatus.eq(MemberCouponStatus.USED))),
					"usedQuantity" // 사용 완료(USED)된 쿠폰 개수 바인딩
				),
				coupon.expirationAt
			))
			.from(coupon)
			.fetch();
	}
}
