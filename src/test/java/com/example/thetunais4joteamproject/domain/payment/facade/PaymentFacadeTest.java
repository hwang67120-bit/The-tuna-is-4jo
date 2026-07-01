package com.example.thetunais4joteamproject.domain.payment.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.thetunais4joteamproject.domain.cart.service.CartService;
import com.example.thetunais4joteamproject.domain.coupon.dto.UseCouponRequest;
import com.example.thetunais4joteamproject.domain.coupon.service.CouponService;
import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderStatus;
import com.example.thetunais4joteamproject.domain.order.repository.OrderRepository;
import com.example.thetunais4joteamproject.domain.order.service.OrderService;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmRequest;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.example.thetunais4joteamproject.domain.payment.repository.PaymentRepository;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentCommandService;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentConfirmTransactionService;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.global.error.BusinessException;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private PaymentGateway paymentGateway;

	@Mock
	private PaymentGatewayResponse pgResponse;

	@Mock
	private OrderService orderService;

	@Mock
	private CartService cartService;

	@Mock
	private CouponService couponService;

	@Mock
	private Member member;

	@Mock
	private Member otherMember;

	private PaymentCommandService paymentCommandService;
	private PaymentConfirmTransactionService paymentConfirmTransactionService;
	private PaymentFacade paymentFacade;

	@BeforeEach
	void setUp() {
		paymentCommandService = new PaymentCommandService(paymentRepository);
		paymentConfirmTransactionService = new PaymentConfirmTransactionService(
			paymentCommandService,
			paymentRepository,
			orderRepository,
			orderService,
			cartService,
			couponService
		);
		paymentFacade = new PaymentFacade(
			paymentGateway,
			paymentConfirmTransactionService
		);
	}

	@Test
	void 결제_승인에_성공한다() {
		// given
		Long memberId = 1L;
		Order order = createOrder(1L, member, memberId, 26000);
		Payment payment = createPendingPayment(10L, order, "pay-123", 26000);

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			order.getId(),
			payment.getId(),
			"pay-123"
		);

		given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(paymentRepository.findByIdForUpdate(payment.getId())).willReturn(Optional.of(payment));
		given(paymentGateway.getPayment("pay-123")).willReturn(pgResponse);
		given(pgResponse.status()).willReturn("PAID");
		given(pgResponse.totalAmount()).willReturn(26000);
		given(orderService.getOrderItems(order.getId())).willReturn(List.of());

		// when
		paymentFacade.confirmPayment(memberId, request);

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
		verify(cartService).deleteOrderedCartItems(memberId, List.of());
	}

	@Test
	void 이미_승인된_결제는_멱등성_응답을_반환한다() {
		// given
		Long memberId = 1L;
		Order order = createOrder(1L, member, memberId, 26000);
		Payment payment = createPendingPayment(10L, order, "pay-123", 26000);

		payment.complete();
		order.confirm();

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			order.getId(),
			payment.getId(),
			"pay-123"
		);

		given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(paymentRepository.findByIdForUpdate(payment.getId())).willReturn(Optional.of(payment));
		given(orderService.getOrderItems(order.getId())).willReturn(List.of());

		// when
		paymentFacade.confirmPayment(memberId, request);

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
		verify(paymentGateway, never()).getPayment("pay-123");
		verify(cartService).deleteOrderedCartItems(memberId, List.of());
	}

	@Test
	void 결제_승인에_성공하면_선택한_사용자_쿠폰을_사용_처리한다() {
		// given
		Long memberId = 1L;
		Long memberCouponId = 30L;
		Order order = createOrder(1L, member, memberId, memberCouponId, 26000);
		Payment payment = createPendingPayment(10L, order, "pay-123", 26000);

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			order.getId(),
			payment.getId(),
			"pay-123"
		);

		given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(paymentRepository.findByIdForUpdate(payment.getId())).willReturn(Optional.of(payment));
		given(paymentGateway.getPayment("pay-123")).willReturn(pgResponse);
		given(pgResponse.status()).willReturn("PAID");
		given(pgResponse.totalAmount()).willReturn(26000);
		given(orderService.getOrderItems(order.getId())).willReturn(List.of());

		// when
		paymentFacade.confirmPayment(memberId, request);

		// then
		verify(couponService).useCoupon(
			memberId,
			new UseCouponRequest(memberCouponId, order.getOrderPrice())
		);
	}

	@Test
	void PG_상태가_PAID가_아니면_결제_실패_처리한다() {
		// given
		Long memberId = 1L;
		Order order = createOrder(1L, member, memberId, 26000);
		Payment payment = createPendingPayment(10L, order, "pay-123", 26000);

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			order.getId(),
			payment.getId(),
			"pay-123"
		);

		given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(paymentRepository.findByIdForUpdate(payment.getId())).willReturn(Optional.of(payment));
		given(paymentGateway.getPayment("pay-123")).willReturn(pgResponse);
		given(pgResponse.status()).willReturn("FAILED");

		// when & then
		assertThatThrownBy(() -> paymentFacade.confirmPayment(memberId, request))
			.isInstanceOf(BusinessException.class);

		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
	}

	@Test
	void PG_결제_금액이_다르면_결제_실패_처리한다() {
		// given
		Long memberId = 1L;
		Order order = createOrder(1L, member, memberId, 26000);
		Payment payment = createPendingPayment(10L, order, "pay-123", 26000);

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			order.getId(),
			payment.getId(),
			"pay-123"
		);

		given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));
		given(paymentRepository.findByIdForUpdate(payment.getId())).willReturn(Optional.of(payment));
		given(paymentGateway.getPayment("pay-123")).willReturn(pgResponse);
		given(pgResponse.status()).willReturn("PAID");
		given(pgResponse.totalAmount()).willReturn(25000);

		// when & then
		assertThatThrownBy(() -> paymentFacade.confirmPayment(memberId, request))
			.isInstanceOf(BusinessException.class);

		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
	}

	@Test
	void 주문과_결제가_매칭되지_않으면_예외가_발생한다() {
		// given
		Long memberId = 1L;

		Order requestOrder = createOrder(1L, member, memberId, 26000);
		Order paymentOrder = createOrder(2L, member, memberId, 26000);

		Payment payment = createPendingPayment(10L, paymentOrder, "pay-123", 26000);

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			requestOrder.getId(),
			payment.getId(),
			"pay-123"
		);

		given(orderRepository.findById(requestOrder.getId())).willReturn(Optional.of(requestOrder));
		given(paymentRepository.findById(payment.getId())).willReturn(Optional.of(payment));

		// when & then
		assertThatThrownBy(() -> paymentFacade.confirmPayment(memberId, request))
			.isInstanceOf(BusinessException.class);

		verify(paymentGateway, never()).getPayment("pay-123");
	}

	@Test
	void 다른_회원의_주문이면_예외가_발생한다() {
		// given
		Long ownerId = 1L;
		Long requestMemberId = 999L;

		Order order = createOrder(1L, member, ownerId, 26000);

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			order.getId(),
			10L,
			"pay-123"
		);

		given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

		// when & then
		assertThatThrownBy(() -> paymentFacade.confirmPayment(requestMemberId, request))
			.isInstanceOf(BusinessException.class);

		verify(paymentRepository, never()).findById(10L);
		verify(paymentGateway, never()).getPayment("pay-123");
	}

	private Order createOrder(Long orderId, Member member, Long memberId, Integer totalAmount) {
		return createOrder(orderId, member, memberId, null, totalAmount);
	}

	private Order createOrder(
		Long orderId,
		Member member,
		Long memberId,
		Long memberCouponId,
		Integer totalAmount
	) {
		given(member.getId()).willReturn(memberId);

		Order order = Order.of(
			member,
			"ORD-1234567890",
			memberCouponId,
			totalAmount,
			0,
			0,
			totalAmount
		);

		ReflectionTestUtils.setField(order, "id", orderId);

		return order;
	}

	private Payment createPendingPayment(
		Long paymentId,
		Order order,
		String portonePaymentId,
		Integer amount
	) {
		Payment payment = Payment.createPendingPayment(
			order,
			portonePaymentId,
			amount,
			amount
		);

		ReflectionTestUtils.setField(payment, "id", paymentId);

		return payment;
	}
}
