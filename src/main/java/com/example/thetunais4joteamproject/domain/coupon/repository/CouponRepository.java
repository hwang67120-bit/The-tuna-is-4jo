package com.example.thetunais4joteamproject.domain.coupon.repository;

import com.example.thetunais4joteamproject.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {
}
