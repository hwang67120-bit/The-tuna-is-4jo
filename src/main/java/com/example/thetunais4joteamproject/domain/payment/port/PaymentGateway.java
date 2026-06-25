package com.example.thetunais4joteamproject.domain.payment.port;

public interface PaymentGateway {

	// PG사에서 실제 결제 정보 조회 (금액 검증용)
	PaymentGatewayResponse getPayment(String paymentId);

	void cancelPayment(String paymentId, String reason);

}
