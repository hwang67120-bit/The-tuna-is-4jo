package com.example.thetunais4joteamproject.domain.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;

	// order 검증이 완료되었다는 가정 하에 작성하는 메서드 입니다.
	@Transactional
	public Payment createPayment(/*Order order*/){
		// 쿠폰가를 제외한 실 결제 금액 계산, 아래 코드는 추후 제거 예정
		// todo - 아래 코드 제거 후 주석 해제
		Integer pgAmount = 0;
		//Integer pgAmount = order.getTotalAmount() - 쿠폰가격
		
		// Portone Id 생성
		String portonePaymentId = "pay-" + UUID.randomUUID();

		// todo - 주석 제거
		Payment payment = Payment.createPendingPayment(/*order, */portonePaymentId, /*order.getTotalAmount, */ pgAmount);

		return paymentRepository.save(payment);
	}
}
