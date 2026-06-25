package com.example.thetunais4joteamproject.domain.infra.portone.dto;

import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentResponse(
	String id,                            // 결제 건 ID (우리가 생성한 paymentId)
	String status,                        // 결제 상태: READY, PAID, FAILED, CANCELLED, PARTIAL_CANCELLED
	PaymentAmount amount                  // 결제 금액 세부 정보
) {

	public PaymentGatewayResponse toGatewayResponse() {
		return new PaymentGatewayResponse(
			id,
			status,
			amount.total()
		);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PaymentAmount(
		int total     // 총 결제 금액
	) {
	}
}