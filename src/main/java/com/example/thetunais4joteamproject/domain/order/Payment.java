package com.example.thetunais4joteamproject.domain.order;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import java.time.LocalDateTime;

public class Payment extends BaseEntity {

    private Long id;
    private Long orderId;
    private String portonePaymentId;
    private Integer requestedAmount;
    private Integer paidAmount;
    private String paymentMethod;
    private String status;
    private LocalDateTime paidAt;
}
