package com.example.thetunais4joteamproject.domain.refund.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;
import com.example.thetunais4joteamproject.domain.refund.dto.RefundRequest;
import com.example.thetunais4joteamproject.domain.refund.dto.RefundResponse;
import com.example.thetunais4joteamproject.domain.refund.entity.Refund;
import com.example.thetunais4joteamproject.domain.refund.entity.RefundStatus;
import com.example.thetunais4joteamproject.domain.refund.repository.RefundRepository;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundService {

	private final RefundRepository refundRepository;
	private final PaymentRepository paymentRepository;
	private final MemberRepository memberRepository;
	private final PaymentGateway paymentGateway;

	@Transactional
	public RefundResponse requestRefund(Long memberId, RefundRequest request) {
		Payment payment = paymentRepository.findById(request.paymentId())
			.orElseThrow(() -> BusinessException.from(ErrorCode.PAYMENT_NOT_FOUND));

		Member requester = memberRepository.findById(memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

		validateRefundRequest(memberId, payment, request.refundAmount());

		Refund refund = Refund.create(payment, requester, request.reason(), request.refundAmount());

		return RefundResponse.from(refundRepository.save(refund));
	}

	@Transactional
	public RefundResponse approveRefund(Long adminId, Long refundId) {
		Refund refund = refundRepository.findById(refundId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.REFUND_NOT_FOUND));

		Member admin = memberRepository.findById(adminId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

		if (refund.getStatus() != RefundStatus.REQUESTED) {
			throw BusinessException.from(ErrorCode.INVALID_REFUND_STATUS);
		}

		Payment payment = refund.getPayment();

		if (payment.getStatus() != PaymentStatus.PAID) {
			throw BusinessException.from(ErrorCode.INVALID_PAYMENT_STATUS);
		}

		try {
			paymentGateway.cancelPayment(payment.getPortonePaymentId(), refund.getReason());

			payment.refund();
			refund.complete(admin);

			return RefundResponse.from(refund);

		} catch (Exception e) {
			if (refund.getStatus() == RefundStatus.REQUESTED) {
				refund.fail(e.getMessage());
			}

			return RefundResponse.from(refund);
		}
	}

	@Transactional
	public RefundResponse rejectRefund(Long adminId, Long refundId, String rejectionReason) {
		Refund refund = refundRepository.findById(refundId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.REFUND_NOT_FOUND));

		Member admin = memberRepository.findById(adminId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

		if (refund.getStatus() != RefundStatus.REQUESTED) {
			throw BusinessException.from(ErrorCode.INVALID_REFUND_STATUS);
		}

		refund.reject(admin, rejectionReason);

		return RefundResponse.from(refund);
	}

	private void validateRefundRequest(Long memberId, Payment payment, Integer refundAmount) {
		if (payment.getStatus() != PaymentStatus.PAID) {
			throw BusinessException.from(ErrorCode.INVALID_PAYMENT_STATUS);
		}

		if (!payment.getOrder().getMember().getId().equals(memberId)) {
			throw BusinessException.from(ErrorCode.FORBIDDEN);
		}

		if (!refundAmount.equals(payment.getPgAmount())) {
			throw BusinessException.from(ErrorCode.INVALID_REFUND_AMOUNT);
		}

		boolean existsRefund = refundRepository.existsByPaymentIdAndStatusIn(payment.getId(),
			List.of(RefundStatus.REQUESTED, RefundStatus.COMPLETED));

		if (existsRefund) {
			throw BusinessException.from(ErrorCode.ALREADY_REQUESTED_REFUND);
		}
	}
}