package com.example.thetunais4joteamproject.domain.coupon;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import java.time.LocalDateTime;

public class Coupon extends BaseEntity {

    private Long id;
    private String name;
    private String discountType;
    private Integer discountValue;
    private Integer minimumOrderAmount;
    private Integer maximumDiscountAmount;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private Integer maxPerMember;
    private LocalDateTime issueStartAt;
    private LocalDateTime issueEndAt;
    private LocalDateTime expiresAt;
    private String status;
}
