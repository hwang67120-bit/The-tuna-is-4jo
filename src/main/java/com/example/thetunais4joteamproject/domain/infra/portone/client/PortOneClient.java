package com.example.thetunais4joteamproject.domain.infra.portone.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.thetunais4joteamproject.domain.infra.portone.config.PortOneProperties;
import com.example.thetunais4joteamproject.domain.infra.portone.dto.PortOneCancelRequest;
import com.example.thetunais4joteamproject.domain.infra.portone.dto.PortOnePaymentResponse;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneClient implements PaymentGateway {

	private final RestClient portOneRestClient;
	private final PortOneProperties portOneProperties;

	// 결제 조회
	@Override
	public PaymentGatewayResponse getPayment(String paymentId) {
		log.info("PortOne 결제 조회: {}", paymentId);

		// GET 요청 구조: api/payments/{paymentId}?storeId={storeId}
		PortOnePaymentResponse response = portOneRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/payments/{paymentId}") // URL 동적 생성
				.queryParam("storeId", portOneProperties.getStoreId())
				.build(paymentId))
			.retrieve()
			.body(PortOnePaymentResponse.class);

		// PortOnePaymentResponse -> PaymentGatewayResponse 변환
		return response.toGatewayResponse();
	}

	// 결제 전체 환불
	public void cancelPayment(String paymentId, String reason, Integer amount) {
		log.info("PortOne 결제 취소 요청: paymentId={}, amount={}, reason={}", paymentId, amount, reason);

		PortOneCancelRequest request = PortOneCancelRequest.of(
			reason,
			amount,
			portOneProperties.getStoreId()
		);

		portOneRestClient.post()
			.uri("/payments/{paymentId}/cancel", paymentId)
			.body(request)
			.retrieve()
			.toBodilessEntity();
	}
}
