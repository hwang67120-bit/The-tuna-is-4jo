package com.example.thetunais4joteamproject.domain.payment.entity;

import java.time.LocalDateTime;

import com.example.thetunais4joteamproject.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "payment",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_payment_portone_payment_id",
		columnNames = "portone_payment_id"
	)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// //Order Entity가 생성될 때 까지 임시 주석 처리
	// @OneToOne(fetch = FetchType.LAZY, optional = false)
	// @JoinColumn(name = "order_id", nullable = false, unique = true)
	// private Order order;

	@Column(name = "portone_payment_id", length = 50, nullable = false)
	private String portonePaymentId;

	@Column(name = "requested_amount", nullable = false)
	private Integer requestedAmount;

	@Column(name = "pg_amount", nullable = false)
	private Integer pgAmount; // 쿠폰가를 제외한 금액

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	// todo - 주석 제거
	private Payment(/*Order order,*/ String portonePaymentId, Integer requestedAmount, Integer pgAmount,

		PaymentStatus status) {
		// this.order = order;
		this.portonePaymentId = portonePaymentId;
		this.requestedAmount = requestedAmount;
		this.pgAmount = pgAmount;
		this.status = status;
	}

	// todo - 주석 제거
	public static Payment createPendingPayment(
		// Order order,
		String portonePaymentId,
		// Integer requestedAmount,
		Integer pgAmount
	) {
		return new Payment(
			//order,
			portonePaymentId,
			0 /*requestedAmount*/,
			pgAmount,
			PaymentStatus.PENDING
		);
	}

	public void complete() {
		changeStatus(PaymentStatus.PAID);
		this.paidAt = LocalDateTime.now();
	}

	public void fail() {
		changeStatus(PaymentStatus.FAILED);
	}

	public void cancel() {
		changeStatus(PaymentStatus.CANCELED);
	}

	public void refund() {
		changeStatus(PaymentStatus.REFUNDED);
	}

	private void changeStatus(PaymentStatus nextStatus) {
		this.status.validateTransition(nextStatus);
		this.status = nextStatus;
	}

}