package com.example.thetunais4joteamproject.domain.order;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import java.time.LocalDateTime;

public class Orders extends BaseEntity {

    private Long id;
    private Long memberId;
    private Long memberCouponId;
    private String orderNumber;
    private Integer originalAmount;
    private Integer discountAmount;
    private Integer paymentAmount;
    private String status;
    private LocalDateTime canceledAt;
}
