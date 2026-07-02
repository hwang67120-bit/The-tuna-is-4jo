package com.example.thetunais4joteamproject.domain.coupon.repository;

import com.example.thetunais4joteamproject.domain.coupon.dto.MemberCouponInfoResponse;

import java.util.List;

public interface MemberCouponRepositoryCustom {
	List<MemberCouponInfoResponse> findMyCoupons(Long memberId);
}
