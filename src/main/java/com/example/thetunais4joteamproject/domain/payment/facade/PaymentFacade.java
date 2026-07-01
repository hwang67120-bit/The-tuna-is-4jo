package com.example.thetunais4joteamproject.domain.payment.facade;

import org.springframework.stereotype.Component;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmRequest;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmResponse;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentConfirmContext;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentConfirmTransactionService;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {

	private static final String PG_STATUS_PAID = "PAID";

	private final PaymentGateway paymentGateway;
	private final PaymentConfirmTransactionService paymentConfirmTransactionService;

	// order 조회
	// order 소유권 검증 호출
	// payment 조회
	// payment 검증 호출
	// PortOne 조회
	// PortOne 검증 호출
	// 쿠폰 사용 호출
	// payment 승인 호출
	// order 완료 호출
	// 주문에 포함된 장바구니 상품 삭제 호출
	public PaymentConfirmResponse confirmPayment(Long memberId, PaymentConfirmRequest request) {
		PaymentConfirmContext context = paymentConfirmTransactionService.prepare(memberId, request);

		// 멱등성 처리
		if (context.alreadyPaid()) {
			return paymentConfirmTransactionService.complete(memberId, request);
		}

		// PG 결제 조회
		PaymentGatewayResponse pgResponse = paymentGateway.getPayment(context.portonePaymentId());

		// PG 결제 검증
		validatePgPaymentCompleted(context, pgResponse);
		validatePgAmount(context, pgResponse);

		return paymentConfirmTransactionService.complete(memberId, request);
	}

	private void validatePgPaymentCompleted(PaymentConfirmContext context, PaymentGatewayResponse pgResponse) {
		if (!PG_STATUS_PAID.equals(pgResponse.status())) {
			log.error(
				"결제 승인 실패 - PG 상태 비정상: paymentId={}, pgStatus={}",
				context.paymentId(),
				pgResponse.status()
			);

			paymentConfirmTransactionService.fail(context.paymentId());

			throw BusinessException.from(ErrorCode.PAYMENT_NOT_PAID);
		}
	}

	private void validatePgAmount(PaymentConfirmContext context, PaymentGatewayResponse pgResponse) {
		if (!context.pgAmount().equals(pgResponse.totalAmount())) {
			log.error(
				"결제 승인 실패 - PG 금액 불일치: paymentId={}, expected={}, actual={}",
				context.paymentId(),
				context.pgAmount(),
				pgResponse.totalAmount()
			);

			paymentConfirmTransactionService.fail(context.paymentId());

			throw BusinessException.from(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}
	}

}
