package com.example.thetunais4joteamproject.domain.coupon.repository;

import com.example.thetunais4joteamproject.domain.coupon.dto.CouponAdminResponse;

import java.util.List;

public interface CouponRepositoryCustom {
	List<CouponAdminResponse> findCouponsManagementStatus();
}
