package com.example.thetunais4joteamproject.domain.infra.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

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
import com.example.thetunais4joteamproject.domain.refund.entity.Refund;
import com.example.thetunais4joteamproject.domain.refund.entity.RefundStatus;
import com.example.thetunais4joteamproject.domain.refund.repository.RefundRepository;
import com.example.thetunais4joteamproject.domain.user.entity.Member;

@ExtendWith(MockitoExtension.class)
class RefundWebhookServiceTest {

	@Mock
	private RefundRepository refundRepository;

	private RefundWebhookService refundWebhookService;

	@BeforeEach
	void setUp() {
		refundWebhookService = new RefundWebhookService(refundRepository);
	}

	@Test
	void completeRefundByWebhook_REQUESTED_환불이면_결제와_환불을_완료_처리한다() {
		// given
		Payment payment = createPaidPayment(10L, "pay-123", 26000);
		Refund refund = createRefund(100L, payment);

		given(refundRepository.findTopByPaymentIdAndStatusInOrderByRequestedAtDesc(
			org.mockito.ArgumentMatchers.eq(payment.getId()),
			anyList()
		)).willReturn(Optional.of(refund));

		// when
		refundWebhookService.completeRefundByWebhook(payment);

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
	}

	@Test
	void completeRefundByWebhook_FAILED_환불이면_완료로_복구한다() {
		// given
		Payment payment = createPaidPayment(10L, "pay-123", 26000);
		Refund refund = createRefund(100L, payment);
		refund.fail("PG 응답 지연");

		given(refundRepository.findTopByPaymentIdAndStatusInOrderByRequestedAtDesc(
			org.mockito.ArgumentMatchers.eq(payment.getId()),
			anyList()
		)).willReturn(Optional.of(refund));

		// when
		refundWebhookService.completeRefundByWebhook(payment);

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
	}

	@Test
	void completeRefundByWebhook_이미_완료된_환불이면_중복_처리하지_않는다() {
		// given
		Payment payment = createPaidPayment(10L, "pay-123", 26000);
		Refund refund = createRefund(100L, payment);

		payment.refund();
		refund.completeByWebhook();

		given(refundRepository.findTopByPaymentIdAndStatusInOrderByRequestedAtDesc(
			org.mockito.ArgumentMatchers.eq(payment.getId()),
			anyList()
		)).willReturn(Optional.of(refund));

		// when
		refundWebhookService.completeRefundByWebhook(payment);

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
	}

	private Payment createPaidPayment(Long paymentId, String portonePaymentId, Integer amount) {
		Member member = Member.create("user@test.com", "password", "member", "010-0000-0000");
		ReflectionTestUtils.setField(member, "id", 1L);

		Order order = Order.of(member, "ORD-" + paymentId, amount, 0, 0, amount);
		ReflectionTestUtils.setField(order, "id", paymentId);

		Payment payment = Payment.createPendingPayment(order, portonePaymentId, amount, amount);
		ReflectionTestUtils.setField(payment, "id", paymentId);
		payment.complete();

		return payment;
	}

	private Refund createRefund(Long refundId, Payment payment) {
		Refund refund = Refund.create(
			payment,
			payment.getOrder().getMember(),
			"단순 변심",
			payment.getPgAmount()
		);
		ReflectionTestUtils.setField(refund, "id", refundId);

		return refund;
	}
}
