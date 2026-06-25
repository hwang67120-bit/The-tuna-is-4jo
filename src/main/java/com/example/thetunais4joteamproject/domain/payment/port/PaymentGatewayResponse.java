package com.example.thetunais4joteamproject.domain.payment.port;

public record PaymentGatewayResponse(
	String id,
	String status,
	int totalAmount
) {
}
