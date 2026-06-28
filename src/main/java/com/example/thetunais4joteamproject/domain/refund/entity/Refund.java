package com.example.thetunais4joteamproject.domain.refund.entity;

import java.time.LocalDateTime;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // 환불 요청 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    // 처리 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Member admin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "coupon_restored", nullable = false)
    private boolean couponRestored;

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    @Column(name = "portone_cancellation_id", unique = true, length = 50)
    private String portoneCancellationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    private Refund(
        Payment payment,
        Member requester,
        String reason,
        Integer refundAmount
    ) {
        this.payment = payment;
        this.requester = requester;
        this.reason = reason;
        this.refundAmount = refundAmount;
        this.status = RefundStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
        this.couponRestored = false;
    }

    public static Refund create(
        Payment payment,
        Member requester,
        String reason,
        Integer refundAmount
    ) {
        return new Refund(payment, requester, reason, refundAmount);
    }

    public void approve(Member admin, String portoneCancellationId) {
        this.admin = admin;
        this.portoneCancellationId = portoneCancellationId;
        this.status = RefundStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(Member admin, String rejectionReason) {
        this.admin = admin;
        this.rejectionReason = rejectionReason;
        this.status = RefundStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    public void fail(String failureReason) {
        this.failureReason = failureReason;
        this.status = RefundStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    public void restoreCoupon() {
        this.couponRestored = true;
    }
}