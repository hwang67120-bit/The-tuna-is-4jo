package com.example.thetunais4joteamproject.domain.infra.webhook;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.refund.entity.Refund;
import com.example.thetunais4joteamproject.domain.refund.entity.RefundStatus;
import com.example.thetunais4joteamproject.domain.refund.repository.RefundRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundWebhookService {

	private final RefundRepository refundRepository;

	public void completeRefundByWebhook(Payment payment) {

		Refund refund =
			refundRepository.findTopByPaymentIdAndStatusInOrderByRequestedAtDesc(
				payment.getId(),
				List.of(
					RefundStatus.REQUESTED,
					RefundStatus.FAILED,
					RefundStatus.COMPLETED
				)
			).orElseThrow(() ->
				BusinessException.from(ErrorCode.REFUND_NOT_FOUND)
			);

		if (payment.getStatus() != PaymentStatus.PAID &&
			payment.getStatus() != PaymentStatus.REFUNDED) {
			throw BusinessException.from(ErrorCode.INVALID_PAYMENT_STATUS);
		}

		if (payment.getStatus() == PaymentStatus.PAID) {
			payment.refund();
		}

		refund.completeByWebhook();
	}
}