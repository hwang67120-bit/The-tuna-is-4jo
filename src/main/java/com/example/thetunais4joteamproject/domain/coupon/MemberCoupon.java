package com.example.thetunais4joteamproject.domain.coupon;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import java.time.LocalDateTime;

public class MemberCoupon extends BaseEntity {

    private Long id;
    private Long couponId;
    private Long memberId;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expiresAt;
}
