package com.example.thetunais4joteamproject.domain.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentWebhookTransactionService {

	private final PaymentRepository paymentRepository;
	private final PaymentCommandService paymentCommandService;

	@Transactional
	public void completePaidPayment(String portonePaymentId) {
		Payment payment = paymentRepository.findByPortonePaymentIdForUpdate(portonePaymentId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.PAYMENT_NOT_FOUND));

		if (payment.getStatus() == PaymentStatus.PAID) {
			return;
		}

		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw BusinessException.from(ErrorCode.ALREADY_PROCESSED_PAYMENT);
		}

		paymentCommandService.completePayment(payment);
		payment.getOrder().confirm();
	}
}