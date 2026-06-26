package com.example.thetunais4joteamproject.domain.payment.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmRequest;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmResponse;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentCommandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {

	private final PaymentCommandService paymentCommandService;
	private final PaymentGateway paymentGateway;

	// order 조회
	// order 소유권 검증 호출
	// payment 조회
	// payment 검증 호출
	// PortOne 조회
	// PortOne 검증 호출
	// 재고 차감 호출
	// 쿠폰 사용 호출
	// payment 승인 호출
	// order 완료 호출
	// 장바구니 비우기 호출
	@Transactional
	public PaymentConfirmResponse confirmPayment(Long memberId, PaymentConfirmRequest request){
		// order 조회 및 검증

		Payment payment = paymentCommandService.getPayment(request.paymentId());
		paymentCommandService.validatePayment(payment, request.portonePaymentId(), request.orderId().getAmount());

		PaymentGatewayResponse pgResponse = paymentGateway.getPayment(payment.getPortonePaymentId());

	}

	private void vali
}
