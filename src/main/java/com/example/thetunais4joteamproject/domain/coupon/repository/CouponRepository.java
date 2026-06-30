package com.example.thetunais4joteamproject.domain.coupon.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.example.thetunais4joteamproject.domain.coupon.entity.Coupon;
import com.example.thetunais4joteamproject.domain.coupon.entity.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {

	List<Coupon> findByCouponStatusAndExpirationAtAfterOrderByExpirationAtAsc(
		CouponStatus couponStatus,
		LocalDateTime now
	);
}
