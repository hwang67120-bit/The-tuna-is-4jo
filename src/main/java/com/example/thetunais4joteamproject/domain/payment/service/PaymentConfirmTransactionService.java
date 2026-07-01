package com.example.thetunais4joteamproject.domain.payment.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.thetunais4joteamproject.domain.cart.service.CartService;
import com.example.thetunais4joteamproject.domain.coupon.dto.UseCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.service.CouponService;
import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;
import com.example.thetunais4joteamproject.domain.order.repository.OrderRepository;
import com.example.thetunais4joteamproject.domain.order.service.OrderService;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmRequest;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmResponse;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentConfirmTransactionService {

	private final PaymentCommandService paymentCommandService;
	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;
	private final OrderService orderService;
	private final CartService cartService;
	private final CouponService couponService;

	@Transactional(readOnly = true)
	public PaymentConfirmContext prepare(Long memberId, PaymentConfirmRequest request) {
		Order order = getOrder(request.orderId());
		validateOrderOwner(order, memberId);

		Payment payment = paymentCommandService.getPayment(request.paymentId());
		validatePaymentBelongsToOrder(payment, order.getId());

		boolean alreadyPaid = paymentCommandService.isAlreadyPaid(payment);

		if (!alreadyPaid) {
			paymentCommandService.validatePayment(
				payment,
				request.portonePaymentId(),
				order.getTotalAmount()
			);
		}

		return PaymentConfirmContext.from(payment, alreadyPaid);
	}

	@Transactional
	public PaymentConfirmResponse complete(Long memberId, PaymentConfirmRequest request) {
		Payment payment = getPaymentForUpdate(request.paymentId());
		Order order = payment.getOrder();

		validateOrderOwner(order, memberId);
		validatePaymentBelongsToOrder(payment, request.orderId());

		if (paymentCommandService.isAlreadyPaid(payment)) {
			deleteOrderedCartItems(memberId, order);

			return PaymentConfirmResponse.of(payment, "이미 승인된 결제입니다.");
		}

		paymentCommandService.validatePayment(
			payment,
			request.portonePaymentId(),
			order.getTotalAmount()
		);

		useCoupon(memberId, order);
		paymentCommandService.completePayment(payment);
		order.confirm();
		deleteOrderedCartItems(memberId, order);

		return PaymentConfirmResponse.of(payment, "결제가 승인되었습니다.");
	}

	@Transactional
	public void fail(Long paymentId) {
		Payment payment = getPaymentForUpdate(paymentId);

		paymentCommandService.failPayment(payment);
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ORDER_NOT_FOUND));
	}

	private Payment getPaymentForUpdate(Long paymentId) {
		return paymentRepository.findByIdForUpdate(paymentId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.PAYMENT_NOT_FOUND));
	}

	private void validateOrderOwner(Order order, Long memberId) {
		if (!order.getMember().getId().equals(memberId)) {
			throw BusinessException.from(ErrorCode.FORBIDDEN);
		}
	}

	private void validatePaymentBelongsToOrder(Payment payment, Long orderId) {
		if (!Objects.equals(payment.getOrder().getId(), orderId)) {
			throw BusinessException.from(ErrorCode.PAYMENT_ORDER_MISMATCH);
		}
	}

	private void deleteOrderedCartItems(Long memberId, Order order) {
		List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
		List<Long> cartItemIds = orderService.getCartItemIds(orderItems);

		cartService.deleteOrderedCartItems(memberId, cartItemIds);
	}

	private void useCoupon(Long memberId, Order order) {
		if (order.getMemberCouponId() != null) {
			couponService.useCoupon(
				memberId,
				new UseCouponRequest(order.getMemberCouponId(), order.getOrderPrice())
			);
		}
	}
}
