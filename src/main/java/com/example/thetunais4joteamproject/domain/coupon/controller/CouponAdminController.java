package com.example.thetunais4joteamproject.domain.coupon.controller;

import java.util.List;

import com.example.thetunais4joteamproject.domain.coupon.dto.CouponAdminResponse;
import com.example.thetunais4joteamproject.domain.coupon.dto.CreateCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.CreateCouponResponse;
import com.example.thetunais4joteamproject.domain.coupon.service.CouponService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/coupons")
public class CouponAdminController {

    private final CouponService couponService;

    /**
     * [POST] 관리자 쿠폰 생성 정책 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCouponResponse>> createCoupon(
            @Valid
            @RequestBody
            CreateCouponRequest request
    ) {
        Long couponId = couponService.createCoupon(request);
        CreateCouponResponse responseData = new CreateCouponResponse(couponId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(responseData));
    }

    /**
     * [GET] 어드민 쿠폰 발급 및 사용 현황 모니터링
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CouponAdminResponse>>> getCouponsManagementStatus() {
        List<CouponAdminResponse> responseData = couponService.getCouponsManagementStatus();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(responseData));
    }
}
