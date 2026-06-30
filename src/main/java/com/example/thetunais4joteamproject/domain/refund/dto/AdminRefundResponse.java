package com.example.thetunais4joteamproject.domain.refund.dto;

import java.time.LocalDateTime;

import com.example.thetunais4joteamproject.domain.refund.entity.Refund;
import com.example.thetunais4joteamproject.domain.refund.entity.RefundStatus;

public record AdminRefundResponse(
	Long refundId,
	Long paymentId,
	String orderNumber,
	String requesterName,
	String requesterEmail,
	Integer refundAmount,
	String reason,
	RefundStatus status,
	LocalDateTime requestedAt,
	LocalDateTime processedAt
) {

	public static AdminRefundResponse from(Refund refund) {
		return new AdminRefundResponse(
			refund.getId(),
			refund.getPayment().getId(),
			refund.getPayment().getOrder().getOrderNumber(),
			refund.getRequester().getName(),
			refund.getRequester().getEmail(),
			refund.getRefundAmount(),
			refund.getReason(),
			refund.getStatus(),
			refund.getRequestedAt(),
			refund.getProcessedAt()
		);
	}
}
