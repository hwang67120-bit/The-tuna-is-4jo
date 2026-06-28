package com.example.thetunais4joteamproject.domain.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RefundRequest(
	@NotNull Long paymentId,
	@NotBlank String reason,
	@NotNull @Positive Integer refundAmount
) {
}