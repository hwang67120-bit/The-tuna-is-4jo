package com.example.thetunais4joteamproject.domain.refund;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import java.time.LocalDateTime;

public class Refund extends BaseEntity {

    private Long id;
    private Long paymentId;
    private Long requesterId;
    private Long adminId;
    private String reason;
    private String rejectionReason;
    private String failureReason;
    private Boolean couponRestored;
    private Integer refundAmount;
    private String portoneCancellationId;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}
