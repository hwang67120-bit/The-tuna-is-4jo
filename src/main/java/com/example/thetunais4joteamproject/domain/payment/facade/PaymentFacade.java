package com.example.thetunais4joteamproject.domain.payment.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.repository.OrderRepository;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmRequest;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmResponse;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentCommandService;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {

	private static final String PG_STATUS_PAID = "PAID";

	private final PaymentCommandService paymentCommandService;
	private final PaymentGateway paymentGateway;
	private final OrderRepository orderRepository;

	// order 조회
	// order 소유권 검증 호출
	// payment 조회
	// payment 검증 호출
	// PortOne 조회
	// PortOne 검증 호출
	// 재고 차감 호출
	// 쿠폰 사용 호출
	// payment 승인 호출
	// order 완료 호출
	// 장바구니 비우기 호출
	@Transactional
	public PaymentConfirmResponse confirmPayment(Long memberId, PaymentConfirmRequest request) {
		Order order = getOrder(request.orderId());
		validateOrderOwner(order, memberId);

		Payment payment = paymentCommandService.getPayment(request.paymentId());

		// 멱등성 처리
		if (paymentCommandService.isAlreadyPaid(payment)) {
			return PaymentConfirmResponse.of(payment, "이미 승인된 결제입니다.");
		}

		// 내부 결제 검증
		paymentCommandService.validatePayment(
			payment,
			request.portonePaymentId(),
			order.getTotalAmount()
		);

		// PG 결제 조회
		PaymentGatewayResponse pgResponse = paymentGateway.getPayment(payment.getPortonePaymentId());

		// PG 결제 검증
		validatePgPaymentCompleted(payment, pgResponse);
		validatePgAmount(payment, pgResponse);

		// TODO: 재고 차감 호출
		// TODO: 쿠폰 사용 호출

		paymentCommandService.completePayment(payment);
		order.confirm();

		// TODO: 장바구니 비우기 호출

		return PaymentConfirmResponse.of(payment, "결제가 승인되었습니다.");
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ORDER_NOT_FOUND));
	}

	private void validateOrderOwner(Order order, Long memberId) {
		if (!order.getMember().getId().equals(memberId)) {
			throw BusinessException.from(ErrorCode.FORBIDDEN);
		}
	}

	private void validatePgPaymentCompleted(Payment payment, PaymentGatewayResponse pgResponse) {
		if (!PG_STATUS_PAID.equals(pgResponse.status())) {
			log.error(
				"결제 승인 실패 - PG 상태 비정상: paymentId={}, pgStatus={}",
				payment.getId(),
				pgResponse.status()
			);

			paymentCommandService.failPayment(payment);

			throw BusinessException.from(ErrorCode.PAYMENT_NOT_PAID);
		}
	}

	private void validatePgAmount(Payment payment, PaymentGatewayResponse pgResponse) {
		if (!payment.getPgAmount().equals(pgResponse.totalAmount())) {
			log.error(
				"결제 승인 실패 - PG 금액 불일치: paymentId={}, expected={}, actual={}",
				payment.getId(),
				payment.getPgAmount(),
				pgResponse.totalAmount()
			);

			paymentCommandService.failPayment(payment);

			throw BusinessException.from(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}
	}
}
