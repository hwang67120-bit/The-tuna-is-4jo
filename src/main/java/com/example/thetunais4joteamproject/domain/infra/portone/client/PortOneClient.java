package com.example.thetunais4joteamproject.domain.infra.portone.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.thetunais4joteamproject.domain.infra.portone.config.PortOneProperties;
import com.example.thetunais4joteamproject.domain.infra.portone.dto.PortOneCancelRequest;
import com.example.thetunais4joteamproject.domain.infra.portone.dto.PortOnePaymentResponse;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

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
				.queryParam("storeId", portOneProperties.getStoreId()).build(paymentId))
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
				throw BusinessException.from(ErrorCode.PAYMENT_NOT_FOUND);
			})
			.onStatus(HttpStatusCode::is5xxServerError, (request, clientResponse) -> {
				handleServerError(paymentId);
			})
			.body(PortOnePaymentResponse.class);

		if (response == null) {
			throw BusinessException.from(ErrorCode.PAYMENT_NOT_FOUND);
		}

		// PortOnePaymentResponse -> PaymentGatewayResponse 변환
		return response.toGatewayResponse();
	}

	// 결제 전체 환불
	@Override
	public void cancelPayment(String paymentId, String reason) {
		log.info("PortOne 결제 취소 요청: paymentId={}, reason={}", paymentId, reason);

		PortOneCancelRequest request = PortOneCancelRequest.of(reason, portOneProperties.getStoreId());

		portOneRestClient.post()
			.uri("/payments/{paymentId}/cancel", paymentId)
			.body(request)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, clientResponse) -> {
				handlePaymentNotFound(paymentId);
			})
			.onStatus(HttpStatusCode::is5xxServerError, (req, clientResponse) -> {
				handleServerError(paymentId);
			})
			.toBodilessEntity();
	}

	private void handlePaymentNotFound(String paymentId) {
		log.warn("PortOne 결제 조회 실패: paymentId={}", paymentId);
		throw BusinessException.from(ErrorCode.PAYMENT_NOT_FOUND);
	}

	private void handleServerError(String paymentId) {
		log.error("PortOne 서버 오류: paymentId={}", paymentId);
		throw BusinessException.from(ErrorCode.PG_SERVER_ERROR);
	}
}
