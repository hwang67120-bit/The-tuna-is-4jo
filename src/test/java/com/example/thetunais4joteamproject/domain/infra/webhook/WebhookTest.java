package com.example.thetunais4joteamproject.domain.infra.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentCommandService;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.global.error.BusinessException;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookTransactionCancelledCancelled;
import io.portone.sdk.server.webhook.WebhookTransactionCancelledDataCancelled;
import io.portone.sdk.server.webhook.WebhookTransactionDataPaid;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;

@ExtendWith(MockitoExtension.class)
class WebhookTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentGateway paymentGateway;

	@Mock
	private WebhookEventService webhookEventService;

	@Mock
	private RefundWebhookService refundWebhookService;

	@Mock
	private PortOneWebhookVerifier portOneWebhookVerifier;

	@Mock
	private WebhookTransactionPaid paidWebhook;

	@Mock
	private WebhookTransactionDataPaid paidWebhookData;

	@Mock
	private WebhookTransactionCancelledCancelled cancelledWebhook;

	@Mock
	private WebhookTransactionCancelledDataCancelled cancelledWebhookData;

	private PaymentCommandService paymentCommandService;
	private WebhookHandler webhookHandler;
	private WebhookController webhookController;

	@BeforeEach
	void setUp() {
		paymentCommandService = new PaymentCommandService(paymentRepository);
		webhookHandler = new WebhookHandler(
			paymentCommandService,
			paymentGateway,
			webhookEventService,
			refundWebhookService
		);
		webhookController = new WebhookController(portOneWebhookVerifier, webhookHandler);
	}

	@Test
	void 정상_웹훅이면_결제_완료_처리된다() {
		// given
		String webhookId = "webhook-1";
		String portonePaymentId = "pay-123";
		String rawPayload = "{\"type\":\"Transaction.Paid\"}";

		Payment payment = createPendingPayment(10L, portonePaymentId, 26000);
		WebhookEvent webhookEvent = createWebhookEvent(1L, webhookId, rawPayload);

		givenPaidWebhook(portonePaymentId);
		given(webhookEventService.saveIfNotDuplicate(webhookId, "WebhookTransactionPaid", rawPayload))
			.willReturn(Optional.of(webhookEvent));
		given(paymentGateway.getPayment(portonePaymentId))
			.willReturn(new PaymentGatewayResponse(portonePaymentId, "PAID", 26000));
		given(paymentRepository.findByPortonePaymentId(portonePaymentId))
			.willReturn(Optional.of(payment));

		// when
		webhookHandler.handle(webhookId, paidWebhook, rawPayload);

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
		verify(webhookEventService).markProcessed(webhookEvent.getId());
	}

	@Test
	void 이미_처리된_웹훅이면_중복_처리하지_않는다() {
		// given
		String webhookId = "webhook-1";
		String rawPayload = "{\"type\":\"Transaction.Paid\"}";

		given(webhookEventService.saveIfNotDuplicate(webhookId, "WebhookTransactionPaid", rawPayload))
			.willReturn(Optional.empty());

		// when
		webhookHandler.handle(webhookId, paidWebhook, rawPayload);

		// then
		verify(paymentGateway, never()).getPayment("pay-123");
		verify(paymentRepository, never()).findByPortonePaymentId("pay-123");
	}

	@Test
	void 금액이_다르면_결제_완료_처리하지_않는다() {
		// given
		String webhookId = "webhook-1";
		String portonePaymentId = "pay-123";
		String rawPayload = "{\"type\":\"Transaction.Paid\"}";

		Payment payment = createPendingPayment(10L, portonePaymentId, 26000);
		WebhookEvent webhookEvent = createWebhookEvent(1L, webhookId, rawPayload);

		givenPaidWebhook(portonePaymentId);
		given(webhookEventService.saveIfNotDuplicate(webhookId, "WebhookTransactionPaid", rawPayload))
			.willReturn(Optional.of(webhookEvent));
		given(paymentGateway.getPayment(portonePaymentId))
			.willReturn(new PaymentGatewayResponse(portonePaymentId, "PAID", 25000));
		given(paymentRepository.findByPortonePaymentId(portonePaymentId))
			.willReturn(Optional.of(payment));

		// when
		webhookHandler.handle(webhookId, paidWebhook, rawPayload);

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
		verify(webhookEventService).markFailed(eq(webhookEvent.getId()), contains("db=26000, pg=25000"));
		verify(webhookEventService, never()).markProcessed(webhookEvent.getId());
	}

	@Test
	void PortOne_상태가_PAID가_아니면_처리하지_않는다() {
		// given
		String webhookId = "webhook-1";
		String portonePaymentId = "pay-123";
		String rawPayload = "{\"type\":\"Transaction.Paid\"}";

		WebhookEvent webhookEvent = createWebhookEvent(1L, webhookId, rawPayload);

		givenPaidWebhook(portonePaymentId);
		given(webhookEventService.saveIfNotDuplicate(webhookId, "WebhookTransactionPaid", rawPayload))
			.willReturn(Optional.of(webhookEvent));
		given(paymentGateway.getPayment(portonePaymentId))
			.willReturn(new PaymentGatewayResponse(portonePaymentId, "FAILED", 26000));

		// when
		webhookHandler.handle(webhookId, paidWebhook, rawPayload);

		// then
		verify(paymentRepository, never()).findByPortonePaymentId(portonePaymentId);
		verify(webhookEventService).markIgnored(eq(webhookEvent.getId()), contains("FAILED"));
		verify(webhookEventService, never()).markProcessed(webhookEvent.getId());
	}

	@Test
	void 존재하지_않는_결제면_실패한다() {
		// given
		String webhookId = "webhook-1";
		String portonePaymentId = "pay-123";
		String rawPayload = "{\"type\":\"Transaction.Paid\"}";

		WebhookEvent webhookEvent = createWebhookEvent(1L, webhookId, rawPayload);

		givenPaidWebhook(portonePaymentId);
		given(webhookEventService.saveIfNotDuplicate(webhookId, "WebhookTransactionPaid", rawPayload))
			.willReturn(Optional.of(webhookEvent));
		given(paymentGateway.getPayment(portonePaymentId))
			.willReturn(new PaymentGatewayResponse(portonePaymentId, "PAID", 26000));
		given(paymentRepository.findByPortonePaymentId(portonePaymentId))
			.willReturn(Optional.empty());

		// when
		webhookHandler.handle(webhookId, paidWebhook, rawPayload);

		// then
		verify(webhookEventService).markFailed(eq(webhookEvent.getId()), contains("찾을 수"));
		verify(webhookEventService, never()).markProcessed(webhookEvent.getId());
	}

	@Test
	void 서명_검증_실패_시_처리하지_않는다() throws Exception {
		// given
		String webhookId = "webhook-1";
		String signature = "invalid-signature";
		String timestamp = "2026-06-28T00:00:00Z";
		String rawPayload = "{\"type\":\"Transaction.Paid\"}";
		WebhookVerificationException exception = org.mockito.Mockito.mock(WebhookVerificationException.class);

		given(portOneWebhookVerifier.verify(rawPayload, webhookId, signature, timestamp))
			.willThrow(exception);

		// when & then
		assertThatThrownBy(() -> webhookController.receiveWebhook(rawPayload, webhookId, signature, timestamp))
			.isInstanceOf(BusinessException.class);

		verify(webhookEventService, never()).saveIfNotDuplicate(webhookId, "WebhookTransactionPaid", rawPayload);
	}

	@Test
	void CANCELLED_웹훅이고_PENDING_결제이면_결제를_취소_처리한다() {
		// given
		String webhookId = "webhook-cancel-1";
		String portonePaymentId = "pay-123";
		String rawPayload = "{\"type\":\"Transaction.Cancelled\"}";

		Payment payment = createPendingPayment(10L, portonePaymentId, 26000);
		WebhookEvent webhookEvent = createWebhookEvent(1L, webhookId, rawPayload);

		givenCancelledWebhook(portonePaymentId);
		given(webhookEventService.saveIfNotDuplicate(webhookId, "WebhookTransactionCancelledCancelled", rawPayload))
			.willReturn(Optional.of(webhookEvent));
		given(paymentGateway.getPayment(portonePaymentId))
			.willReturn(new PaymentGatewayResponse(portonePaymentId, "CANCELLED", 26000));
		given(paymentRepository.findByPortonePaymentId(portonePaymentId))
			.willReturn(Optional.of(payment));
		given(paymentRepository.findByOrderId(payment.getOrder().getId()))
			.willReturn(Optional.of(payment));

		// when
		webhookHandler.handle(webhookId, cancelledWebhook, rawPayload);

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
		verify(refundWebhookService, never()).completeRefundByWebhook(payment);
		verify(webhookEventService).markProcessed(webhookEvent.getId());
	}

	@Test
	void CANCELLED_웹훅이고_PAID_결제이면_환불_웹훅_서비스에_위임한다() {
		// given
		String webhookId = "webhook-cancel-1";
		String portonePaymentId = "pay-123";
		String rawPayload = "{\"type\":\"Transaction.Cancelled\"}";

		Payment payment = createPendingPayment(10L, portonePaymentId, 26000);
		payment.complete();
		WebhookEvent webhookEvent = createWebhookEvent(1L, webhookId, rawPayload);

		givenCancelledWebhook(portonePaymentId);
		given(webhookEventService.saveIfNotDuplicate(webhookId, "WebhookTransactionCancelledCancelled", rawPayload))
			.willReturn(Optional.of(webhookEvent));
		given(paymentGateway.getPayment(portonePaymentId))
			.willReturn(new PaymentGatewayResponse(portonePaymentId, "CANCELLED", 26000));
		given(paymentRepository.findByPortonePaymentId(portonePaymentId))
			.willReturn(Optional.of(payment));

		// when
		webhookHandler.handle(webhookId, cancelledWebhook, rawPayload);

		// then
		verify(refundWebhookService).completeRefundByWebhook(payment);
		verify(webhookEventService).markProcessed(webhookEvent.getId());
	}

	private void givenPaidWebhook(String portonePaymentId) {
		given(paidWebhook.getData()).willReturn(paidWebhookData);
		given(paidWebhookData.getPaymentId()).willReturn(portonePaymentId);
	}

	private void givenCancelledWebhook(String portonePaymentId) {
		given(cancelledWebhook.getData()).willReturn(cancelledWebhookData);
		given(cancelledWebhookData.getPaymentId()).willReturn(portonePaymentId);
	}

	private WebhookEvent createWebhookEvent(Long eventId, String webhookId, String rawPayload) {
		WebhookEvent webhookEvent = new WebhookEvent(webhookId, "WebhookTransactionPaid", rawPayload);
		ReflectionTestUtils.setField(webhookEvent, "id", eventId);

		return webhookEvent;
	}

	private Payment createPendingPayment(Long paymentId, String portonePaymentId, Integer amount) {
		Member member = Member.create("user@test.com", "password", "member", "010-0000-0000");
		ReflectionTestUtils.setField(member, "id", 1L);

		Order order = Order.of(member, "ORD-" + paymentId, amount, 0, 0, amount);
		ReflectionTestUtils.setField(order, "id", paymentId);

		Payment payment = Payment.createPendingPayment(order, portonePaymentId, amount, amount);
		ReflectionTestUtils.setField(payment, "id", paymentId);

		return payment;
	}
}
