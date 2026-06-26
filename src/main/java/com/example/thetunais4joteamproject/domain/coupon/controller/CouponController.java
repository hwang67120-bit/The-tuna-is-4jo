package com.example.thetunais4joteamproject.domain.coupon.controller;

import java.util.List;

import com.example.thetunais4joteamproject.domain.coupon.dto.IssueCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.dto.IssueCouponResponse;
import com.example.thetunais4joteamproject.domain.coupon.dto.MemberCouponInfoResponse;
import com.example.thetunais4joteamproject.domain.coupon.service.CouponService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    /**
     * [POST] 사용자 선착순 쿠폰 발급
     */
    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCoupon(
            @AuthenticationPrincipal
            Long memberId,
            @Valid
            @RequestBody
            IssueCouponRequest request
    ) {
        Long memberCouponId = couponService.issueCoupon(memberId, request);
        IssueCouponResponse responseData = new IssueCouponResponse(memberCouponId);

        // ApiResponse를 적용하여 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(responseData));
    }

    /**
     * 🔍 [GET] 사용자 보유 쿠폰 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberCouponInfoResponse>>> getMyCoupons(
            @AuthenticationPrincipal
            Long memberId
    ) {
        List<MemberCouponInfoResponse> responseData = couponService.getMyCoupons(memberId);

        // ApiResponse.ok 적용, 보유 쿠폰이 0개여도 [] 빈 리스트 반환 방어
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(responseData));
    }
}
