package com.example.thetunais4joteamproject.domain.infra.webhook;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentCommandService;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentConfirmTransactionService;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentWebhookTransactionService;

import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransactionCancelledCancelled;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookHandler {

	private final PaymentCommandService paymentCommandService;
	private final PaymentGateway paymentGateway;
	private final WebhookEventService webhookEventService;
	private final RefundWebhookService refundWebhookService;
	private final PaymentWebhookTransactionService paymentWebhookTransactionService;

	// 웹훅 진입점
	public void handle(String webhookId, Webhook webhook, String rawPayload) {

		// 수신한 웹훅의 "종류"를 문자열로 뽑아냄
		String type = webhook.getClass().getSimpleName();

		// 멱등성
		Optional<WebhookEvent> saved = webhookEventService.saveIfNotDuplicate(webhookId, type, rawPayload);
		if (saved.isEmpty()) {
			return;
		}
		Long eventId = saved.get().getId();

		try {
			if (webhook instanceof WebhookTransactionPaid p) {
				handlePaid(eventId, p.getData().getPaymentId());
			} else if (webhook instanceof WebhookTransactionCancelledCancelled c) {
				handleCancel(eventId, c.getData().getPaymentId());
			} else {
				webhookEventService.markIgnored(eventId, "처리 대상 아님: " + type);
			}
		} catch (Exception e) {
			log.error("[Webhook] failed eventId={}", eventId, e);
			webhookEventService.markFailed(eventId, e.getMessage());
		}
	}

	// 결제 완료 웹훅 처리
	private void handlePaid(Long eventId, String portonePaymentId) {
		PaymentGatewayResponse pg = paymentGateway.getPayment(portonePaymentId);

		if (!"PAID".equals(pg.status())) {
			webhookEventService.markIgnored(eventId, "PG 상태가 PAID가 아님: " + pg.status());
			return;
		}

		Payment payment = paymentCommandService.findByPortonePaymentId(portonePaymentId);

		if (!payment.getPgAmount().equals(pg.totalAmount())) {
			webhookEventService.markFailed(
				eventId,
				"금액 불일치: db=" + payment.getPgAmount() + ", pg=" + pg.totalAmount()
			);
			return;
		}

		paymentWebhookTransactionService.completePaidPayment(portonePaymentId);

		webhookEventService.markProcessed(eventId);
	}

	// 결제 취소
	private void handleCancel(Long eventId, String portonePaymentId) {

		PaymentGatewayResponse pg = paymentGateway.getPayment(portonePaymentId);

		if (!"CANCELLED".equals(pg.status())) {
			webhookEventService.markIgnored(eventId, "PG 상태가 CANCELLED가 아님: " + pg.status());
			return;
		}

		Payment payment = paymentCommandService.findByPortonePaymentId(portonePaymentId);

		if (payment.getStatus() == PaymentStatus.PENDING) {
			paymentCommandService.cancelPayment(payment.getOrder());
		} else if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.REFUNDED) {
			refundWebhookService.completeRefundByWebhook(payment);
		} else {
			webhookEventService.markIgnored(eventId, "처리 대상이 아닌 결제 상태: " + payment.getStatus());
			return;
		}

		webhookEventService.markProcessed(eventId);
	}

}

