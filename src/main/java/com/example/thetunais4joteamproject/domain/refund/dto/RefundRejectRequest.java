package com.example.thetunais4joteamproject.domain.refund.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundRejectRequest(
	@NotBlank String rejectionReason
) {
}