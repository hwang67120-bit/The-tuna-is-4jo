package com.example.thetunais4joteamproject.domain.refund.dto;

import com.example.thetunais4joteamproject.domain.refund.entity.Refund;
import com.example.thetunais4joteamproject.domain.refund.entity.RefundStatus;

public record RefundResponse(
	Long refundId,
	Long paymentId,
	Integer refundAmount,
	RefundStatus status,
	String reason
) {

	public static RefundResponse from(Refund refund) {
		return new RefundResponse(
			refund.getId(),
			refund.getPayment().getId(),
			refund.getRefundAmount(),
			refund.getStatus(),
			refund.getReason()
		);
	}
}