package com.example.thetunais4joteamproject.domain.refund.entity;

import java.time.LocalDateTime;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.global.entity.BaseTimeEntity;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_refund_payment_id", columnNames = "payment_id")
	}
)
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

	public void complete(Member admin) {
		status.validateTransition(RefundStatus.COMPLETED);

		this.admin = admin;
		this.status = RefundStatus.COMPLETED;
		this.processedAt = LocalDateTime.now();
	}

	public void reject(Member admin, String reason) {
		status.validateTransition(RefundStatus.REJECTED);

		this.admin = admin;
		this.rejectionReason = reason;
		this.status = RefundStatus.REJECTED;
		this.processedAt = LocalDateTime.now();
	}

	public void fail(String reason) {
		status.validateTransition(RefundStatus.FAILED);

		this.failureReason = reason;
		this.status = RefundStatus.FAILED;
		this.processedAt = LocalDateTime.now();
	}

	public void restoreCoupon() {
		this.couponRestored = true;
	}

	public void completeByWebhook() {

		if (status == RefundStatus.COMPLETED) {
			return;
		}

		if (status != RefundStatus.REQUESTED &&
			status != RefundStatus.FAILED) {
			throw BusinessException.from(ErrorCode.INVALID_REFUND_STATUS);
		}

		this.status = RefundStatus.COMPLETED;
		this.processedAt = LocalDateTime.now();
	}
}