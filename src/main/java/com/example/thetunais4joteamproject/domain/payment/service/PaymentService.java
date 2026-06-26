package com.example.thetunais4joteamproject.domain.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;

	// order 검증이 완료되었다는 가정 하에 작성하는 메서드 입니다.
	@Transactional
	public Payment createPayment(Order order) {
		Integer requestedAmount = order.getTotalAmount();
		// 쿠폰 도메인 연동 전이므로 PG 요청 금액은 주문 최종 금액과 동일하게 생성합니다.
		Integer pgAmount = order.getTotalAmount();

		// Portone 결제 식별자는 결제 대기 데이터 생성 시점에 미리 발급합니다.
		String portonePaymentId = "pay-" + UUID.randomUUID();

		Payment payment = Payment.createPendingPayment(order, portonePaymentId, requestedAmount, pgAmount);

		return paymentRepository.save(payment);
	}
}
