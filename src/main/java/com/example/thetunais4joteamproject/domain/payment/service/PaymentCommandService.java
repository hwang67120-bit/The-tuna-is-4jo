package com.example.thetunais4joteamproject.domain.payment.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandService {

	private final PaymentRepository paymentRepository;

	public void completePayment(Payment payment) {
		validatePendingStatus(payment);
		payment.complete();

		log.info("결제 승인 완료: paymentId={}", payment.getId());
	}

	public void failPayment(Payment payment) {
		if (payment.getStatus() == PaymentStatus.PAID) {
			throw BusinessException.from(ErrorCode.ALREADY_PROCESSED_PAYMENT);
		}

		if (payment.getStatus() == PaymentStatus.FAILED) {
			return;
		}

		payment.fail();

		log.warn("결제 실패 처리 완료: paymentId={}", payment.getId());
	}


	public Payment getPayment(Long paymentId) {
		return paymentRepository.findById(paymentId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.PAYMENT_NOT_FOUND));
	}

	public boolean isAlreadyPaid(Payment payment) {
		return payment.getStatus() == PaymentStatus.PAID;
	}

	public void validatePayment(Payment payment, String portOnePaymentId, Integer pgAmount) {
		validatePortOnePaymentId(payment, portOnePaymentId);
		validatePendingStatus(payment);
		validateAmount(payment, pgAmount);

	}

	private void validatePortOnePaymentId(Payment payment, String portOnePaymentId) {
		if (!payment.getPortonePaymentId().equals(portOnePaymentId)) {
			log.warn("결제 승인 거부 - portOnePaymentId 불일치: paymentId={}, DB={}", payment.getId(), portOnePaymentId);
			throw BusinessException.from(ErrorCode.PORTONE_PAYMENT_NOT_FOUND);
		}
	}

	private void validatePendingStatus(Payment payment) {
		// PAID 멱등성 검증은 PaymentFacade에서 진행
		PaymentStatus status = payment.getStatus();

		if (status == PaymentStatus.PENDING) {
			return;
		}

		log.warn("결제 승인 거부 - paymentId={}, status={}", payment.getId(), status);

		throw switch (status) {
			case FAILED -> BusinessException.from(ErrorCode.PAYMENT_ALREADY_FAILED);
			case CANCELED -> BusinessException.from(ErrorCode.PAYMENT_ALREADY_CANCELED);
			default -> BusinessException.from(ErrorCode.ALREADY_PROCESSED_PAYMENT);
		};
	}

	private void validateAmount(Payment payment, Integer pgAmount) {
		if (!payment.getPgAmount().equals(pgAmount)) {
			log.warn(
				"결제 승인 거부 - 금액 불일치: paymentId={}, expected={}, actual={}",
				payment.getId(),
				payment.getPgAmount(),
				pgAmount
			);
			throw BusinessException.from(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}
	}
}
