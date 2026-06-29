package com.example.thetunais4joteamproject.domain.order.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.thetunais4joteamproject.domain.address.entity.Address;
import com.example.thetunais4joteamproject.domain.address.repository.AddressRepository;
import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.cart.service.CartService;
import com.example.thetunais4joteamproject.domain.coupon.service.CouponService;
import com.example.thetunais4joteamproject.domain.order.dto.CancelOrderResponse;
import com.example.thetunais4joteamproject.domain.order.dto.CreateCartOrderRequest;
import com.example.thetunais4joteamproject.domain.order.dto.CreateDirectOrderRequest;
import com.example.thetunais4joteamproject.domain.order.dto.CreateOrderResponse;
import com.example.thetunais4joteamproject.domain.order.dto.GetOrderDetailResponse;
import com.example.thetunais4joteamproject.domain.order.dto.GetOrderResponse;
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewResponse;
import com.example.thetunais4joteamproject.domain.order.entity.DeliveryAddress;
import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;
import com.example.thetunais4joteamproject.domain.order.service.OrderService;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentCommandService;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.product.repository.ProductOptionRepository;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrderFacadeTest {

	@Test
	void 선택한_장바구니_상품으로_주문서_미리보기를_생성한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		List<Long> cartItemIds = List.of(10L, 20L);

		CartItem cartItem = createCartItem(
			1L,
			100L,
			"테스트 상품",
			1000L,
			"XL",
			10000,
			1500,
			2
		);

		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(mock(Member.class)));
		when(cartService.getPreviewItems(memberId, cartItemIds))
			.thenReturn(List.of(cartItem));

		// when
		OrderPreviewResponse response = orderFacade.previewOrder(memberId, cartItemIds);

		// then
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).cartItemId()).isEqualTo(1L);
		assertThat(response.items().get(0).productId()).isEqualTo(100L);
		assertThat(response.items().get(0).productName()).isEqualTo("테스트 상품");
		assertThat(response.items().get(0).productOptionId()).isEqualTo(1000L);
		assertThat(response.items().get(0).optionName()).isEqualTo("XL");
		assertThat(response.items().get(0).unitPrice()).isEqualTo(11500);
		assertThat(response.items().get(0).quantity()).isEqualTo(2);
		assertThat(response.items().get(0).totalPrice()).isEqualTo(23000);

		assertThat(response.orderPrice()).isEqualTo(23000);
		assertThat(response.discountPrice()).isEqualTo(0);
		assertThat(response.deliveryPrice()).isEqualTo(3000);
		assertThat(response.totalAmount()).isEqualTo(26000);

		verify(memberRepository).findById(memberId);
		verify(cartService).getPreviewItems(memberId, cartItemIds);
		verify(cartItem.getProductOption()).validateEnoughStock(2);
	}

	@Test
	void 장바구니_상품_ID가_없으면_전체_장바구니_상품으로_주문서_미리보기를_생성한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;

		CartItem cartItem = createCartItem(
			1L,
			100L,
			"테스트 상품",
			1000L,
			"XL",
			10000,
			1500,
			2
		);

		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(mock(Member.class)));
		when(cartService.getPreviewItems(memberId, List.of()))
			.thenReturn(List.of(cartItem));

		// when
		OrderPreviewResponse response = orderFacade.previewOrder(memberId, null);

		// then
		assertThat(response.items()).hasSize(1);
		assertThat(response.orderPrice()).isEqualTo(23000);
		assertThat(response.discountPrice()).isEqualTo(0);
		assertThat(response.deliveryPrice()).isEqualTo(3000);
		assertThat(response.totalAmount()).isEqualTo(26000);

		verify(memberRepository).findById(memberId);
		verify(cartService).getPreviewItems(memberId, List.of());
		verify(cartItem.getProductOption()).validateEnoughStock(2);
	}

	@Test
	void 장바구니_상품으로_주문과_결제대기_데이터를_생성한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		Member member = mock(Member.class);
		Address address = createAddress(member);
		List<Long> cartItemIds = List.of(1L);
		CreateCartOrderRequest request = new CreateCartOrderRequest(cartItemIds, null);

		CartItem cartItem = createCartItem(
			1L,
			100L,
			"테스트 상품",
			1000L,
			"XL",
			10000,
			1500,
			2
		);

		Order order = createOrder(10L, member, "ORD-1234567890", 23000, 0, 3000, 26000);
		OrderItem orderItem = createOrderItem(
			100L,
			order,
			cartItem.getProductOption(),
			1L,
			100L,
			"테스트 상품",
			"XL",
			11500,
			2
		);
		Payment payment = createPayment(20L, order, "pay-123", 26000);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(addressRepository.findFirstByMemberIdAndDefaultAddressTrue(memberId))
			.thenReturn(Optional.of(address));
		when(cartService.getPreviewItems(memberId, cartItemIds)).thenReturn(List.of(cartItem));
		when(orderService.createOrder(eq(member), eq(23000), eq(0), eq(3000), eq(26000), any(DeliveryAddress.class)))
			.thenReturn(order);
		when(orderService.createOrderItemsFromCartItems(order, List.of(cartItem))).thenReturn(List.of(orderItem));
		when(paymentCommandService.createPayment(order)).thenReturn(payment);

		// when
		CreateOrderResponse response = orderFacade.createCartOrder(memberId, request);

		// then
		assertThat(response.orderId()).isEqualTo(10L);
		assertThat(response.paymentId()).isEqualTo(20L);
		assertThat(response.orderPrice()).isEqualTo(23000);
		assertThat(response.discountPrice()).isEqualTo(0);
		assertThat(response.deliveryPrice()).isEqualTo(3000);
		assertThat(response.totalAmount()).isEqualTo(26000);
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).orderItemId()).isEqualTo(100L);

		verify(cartItem.getProductOption()).decreaseStock(2);
		verify(orderService).createOrderItemsFromCartItems(order, List.of(cartItem));
		verify(paymentCommandService).createPayment(order);
	}

	@Test
	void 사용자_쿠폰을_선택하면_장바구니_주문에_할인금액을_적용한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		Long memberCouponId = 30L;
		Member member = mock(Member.class);
		Address address = createAddress(member);
		List<Long> cartItemIds = List.of(1L);
		CreateCartOrderRequest request = new CreateCartOrderRequest(cartItemIds, memberCouponId);

		CartItem cartItem = createCartItem(
			1L,
			100L,
			"테스트 상품",
			1000L,
			"XL",
			10000,
			1500,
			2
		);

		Order order = createOrder(10L, member, "ORD-1234567890", 23000, 5000, 3000, 21000);
		OrderItem orderItem = createOrderItem(
			100L,
			order,
			cartItem.getProductOption(),
			1L,
			100L,
			"테스트 상품",
			"XL",
			11500,
			2
		);
		Payment payment = createPayment(20L, order, "pay-123", 21000);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(addressRepository.findFirstByMemberIdAndDefaultAddressTrue(memberId))
			.thenReturn(Optional.of(address));
		when(cartService.getPreviewItems(memberId, cartItemIds)).thenReturn(List.of(cartItem));
		when(couponService.calculateDiscountPrice(memberId, memberCouponId, 23000)).thenReturn(5000);
		when(orderService.createOrder(eq(member), eq(memberCouponId), eq(23000), eq(5000), eq(3000), eq(21000), any(DeliveryAddress.class)))
			.thenReturn(order);
		when(orderService.createOrderItemsFromCartItems(order, List.of(cartItem))).thenReturn(List.of(orderItem));
		when(paymentCommandService.createPayment(order)).thenReturn(payment);

		// when
		CreateOrderResponse response = orderFacade.createCartOrder(memberId, request);

		// then
		assertThat(response.orderPrice()).isEqualTo(23000);
		assertThat(response.discountPrice()).isEqualTo(5000);
		assertThat(response.deliveryPrice()).isEqualTo(3000);
		assertThat(response.totalAmount()).isEqualTo(21000);

		verify(couponService).calculateDiscountPrice(memberId, memberCouponId, 23000);
		verify(orderService).createOrder(eq(member), eq(memberCouponId), eq(23000), eq(5000), eq(3000), eq(21000), any(DeliveryAddress.class));
	}

	@Test
	void 결제대기_주문이_있으면_장바구니_주문을_다시_생성하지_않는다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		Member member = mock(Member.class);
		List<Long> cartItemIds = List.of(1L);
		CreateCartOrderRequest request = new CreateCartOrderRequest(cartItemIds, null);
		CartItem cartItem = createCartItem(
			1L,
			100L,
			"테스트 상품",
			1000L,
			"XL",
			10000,
			1500,
			2
		);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(cartService.getPreviewItems(memberId, cartItemIds)).thenReturn(List.of(cartItem));
		doThrow(BusinessException.from(ErrorCode.ORDER_ALREADY_PENDING))
			.when(orderService)
			.validateNoPendingCartOrder(memberId, List.of(cartItem));

		// when & then
		assertThatThrownBy(() -> orderFacade.createCartOrder(memberId, request))
			.isInstanceOf(BusinessException.class);

		verify(cartItem.getProductOption(), never()).decreaseStock(2);
		verify(paymentCommandService, never()).createPayment(any());
	}

	@Test
	void 상품_옵션으로_바로_주문과_결제대기_데이터를_생성한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		Member member = mock(Member.class);
		Address address = createAddress(member);
		CreateDirectOrderRequest request = new CreateDirectOrderRequest(1000L, 2, null);
		ProductOption productOption = createProductOption(
			1000L,
			100L,
			"테스트 상품",
			"XL",
			10000,
			1500
		);

		Order order = createOrder(10L, member, "ORD-1234567890", 23000, 0, 3000, 26000);
		OrderItem orderItem = createOrderItem(
			100L,
			order,
			productOption,
			null,
			100L,
			"테스트 상품",
			"XL",
			11500,
			2
		);
		Payment payment = createPayment(20L, order, "pay-123", 26000);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(addressRepository.findFirstByMemberIdAndDefaultAddressTrue(memberId))
			.thenReturn(Optional.of(address));
		when(productOptionRepository.findById(1000L)).thenReturn(Optional.of(productOption));
		when(orderService.createOrder(eq(member), eq(23000), eq(0), eq(3000), eq(26000), any(DeliveryAddress.class)))
			.thenReturn(order);
		when(orderService.createOrderItems(eq(order), any(), any())).thenReturn(List.of(orderItem));
		when(paymentCommandService.createPayment(order)).thenReturn(payment);

		// when
		CreateOrderResponse response = orderFacade.createDirectOrder(memberId, request);

		// then
		assertThat(response.orderId()).isEqualTo(10L);
		assertThat(response.paymentId()).isEqualTo(20L);
		assertThat(response.totalAmount()).isEqualTo(26000);
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).productOptionId()).isEqualTo(1000L);

		verify(productOption).decreaseStock(2);
		verify(orderService).createOrderItems(order, List.of(productOption), List.of(2));
		verify(paymentCommandService).createPayment(order);
	}

	@Test
	void 주문을_취소하면_재고를_원복하고_결제대기를_취소한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		Long orderId = 10L;
		Member member = mock(Member.class);
		ProductOption productOption = createProductOption(
			1000L,
			100L,
			"테스트 상품",
			"XL",
			10000,
			1500
		);
		Order order = createOrder(orderId, member, "ORD-1234567890", 23000, 0, 3000, 26000);
		OrderItem orderItem = createOrderItem(
			100L,
			order,
			productOption,
			1L,
			100L,
			"테스트 상품",
			"XL",
			11500,
			2
		);
		Payment payment = createPayment(20L, order, "pay-123", 26000);
		payment.cancel();

		when(orderService.getOrder(memberId, orderId)).thenReturn(order);
		when(orderService.getOrderItems(orderId)).thenReturn(List.of(orderItem));
		when(paymentCommandService.cancelPayment(order)).thenReturn(payment);

		// when
		CancelOrderResponse response = orderFacade.cancelOrder(memberId, orderId);

		// then
		assertThat(response.orderId()).isEqualTo(orderId);
		assertThat(response.orderStatus()).isEqualTo("CANCELED");
		assertThat(response.paymentStatus()).isEqualTo("CANCELED");

		verify(productOption).increaseStock(2);
		verify(paymentCommandService).cancelPayment(order);
	}

	@Test
	void 결제완료_주문은_주문취소로_취소할_수_없다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		Long orderId = 10L;
		Member member = mock(Member.class);
		ProductOption productOption = createProductOption(
			1000L,
			100L,
			"테스트 상품",
			"XL",
			10000,
			1500
		);
		Order order = createOrder(orderId, member, "ORD-1234567890", 23000, 0, 3000, 26000);
		order.confirm();
		OrderItem orderItem = createOrderItem(
			100L,
			order,
			productOption,
			1L,
			100L,
			"테스트 상품",
			"XL",
			11500,
			2
		);

		when(orderService.getOrder(memberId, orderId)).thenReturn(order);
		when(orderService.getOrderItems(orderId)).thenReturn(List.of(orderItem));

		// when & then
		assertThatThrownBy(() -> orderFacade.cancelOrder(memberId, orderId))
			.isInstanceOf(BusinessException.class);

		verify(productOption, never()).increaseStock(2);
		verify(paymentCommandService, never()).cancelPayment(order);
	}

	@Test
	void 결제대기_시간이_지난_주문을_만료하고_재고를_원복한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Member member = mock(Member.class);
		ProductOption productOption = createProductOption(
			1000L,
			100L,
			"테스트 상품",
			"XL",
			10000,
			1500
		);
		Order order = createOrder(10L, member, "ORD-1234567890", 23000, 0, 3000, 26000);
		OrderItem orderItem = createOrderItem(
			100L,
			order,
			productOption,
			1L,
			100L,
			"테스트 상품",
			"XL",
			11500,
			2
		);
		Payment payment = createPayment(20L, order, "pay-123", 26000);
		payment.cancel();

		when(orderService.getExpiredPendingOrders(any(LocalDateTime.class))).thenReturn(List.of(order));
		when(orderService.getOrderItems(order.getId())).thenReturn(List.of(orderItem));
		when(paymentCommandService.expirePayment(order)).thenReturn(payment);

		// when
		orderFacade.expirePendingOrders();

		// then
		assertThat(order.getStatus().name()).isEqualTo("EXPIRED");

		verify(orderService).getExpiredPendingOrders(any(LocalDateTime.class));
		verify(productOption).increaseStock(2);
		verify(paymentCommandService).expirePayment(order);
	}

	@Test
	void 상태와_관계없이_주문_목록을_조회한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		Member member = mock(Member.class);
		Order order = createOrder(10L, member, "ORD-1234567890", 23000, 0, 3000, 26000);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(orderService.getOrders(memberId)).thenReturn(List.of(order));

		// when
		List<GetOrderResponse> response = orderFacade.getAll(memberId);

		// then
		assertThat(response).hasSize(1);
		assertThat(response.get(0).orderId()).isEqualTo(10L);
		assertThat(response.get(0).orderNumber()).isEqualTo("ORD-1234567890");
		assertThat(response.get(0).orderStatus()).isEqualTo("PENDING_PAYMENT");
		assertThat(response.get(0).totalAmount()).isEqualTo(26000);

		verify(memberRepository).findById(memberId);
		verify(orderService).getOrders(memberId);
	}

	@Test
	void 상태와_관계없이_주문_상세를_조회한다() {
		// given
		CartService cartService = mock(CartService.class);
		OrderService orderService = mock(OrderService.class);
		PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		ProductOptionRepository productOptionRepository = mock(ProductOptionRepository.class);
		CouponService couponService = mock(CouponService.class);
		AddressRepository addressRepository = mock(AddressRepository.class);

		OrderFacade orderFacade = new OrderFacade(
			cartService,
			orderService,
			paymentCommandService,
			memberRepository,
			productOptionRepository,
			couponService,
			addressRepository
		);

		Long memberId = 1L;
		Long orderId = 10L;
		Member member = mock(Member.class);
		ProductOption productOption = createProductOption(
			1000L,
			100L,
			"테스트 상품",
			"XL",
			10000,
			1500
		);
		Order order = createOrder(orderId, member, "ORD-1234567890", 23000, 0, 3000, 26000);
		OrderItem orderItem = createOrderItem(
			100L,
			order,
			productOption,
			1L,
			100L,
			"테스트 상품",
			"XL",
			11500,
			2
		);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(orderService.getOrderDetail(memberId, orderId)).thenReturn(order);
		when(orderService.getOrderItems(orderId)).thenReturn(List.of(orderItem));

		// when
		GetOrderDetailResponse response = orderFacade.getOne(memberId, orderId);

		// then
		assertThat(response.orderId()).isEqualTo(orderId);
		assertThat(response.orderStatus()).isEqualTo("PENDING_PAYMENT");
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).orderItemId()).isEqualTo(100L);
		assertThat(response.items().get(0).productName()).isEqualTo("테스트 상품");
		assertThat(response.items().get(0).totalPrice()).isEqualTo(23000);

		verify(memberRepository).findById(memberId);
		verify(orderService).getOrderDetail(memberId, orderId);
		verify(orderService).getOrderItems(orderId);
	}

	private CartItem createCartItem(
		Long cartItemId,
		Long productId,
		String productName,
		Long productOptionId,
		String optionName,
		Integer productPrice,
		Integer additionalPrice,
		Integer quantity
	) {
		CartItem cartItem = mock(CartItem.class);
		ProductOption productOption = mock(ProductOption.class);
		Product product = mock(Product.class);

		when(cartItem.getId()).thenReturn(cartItemId);
		when(cartItem.getProductOption()).thenReturn(productOption);
		when(cartItem.getQuantity()).thenReturn(quantity);

		when(productOption.getId()).thenReturn(productOptionId);
		when(productOption.getOptionName()).thenReturn(optionName);
		when(productOption.getAdditionalPrice()).thenReturn(additionalPrice);
		when(productOption.getProduct()).thenReturn(product);

		when(product.getId()).thenReturn(productId);
		when(product.getName()).thenReturn(productName);
		when(product.getPrice()).thenReturn(productPrice);

		return cartItem;
	}

	private ProductOption createProductOption(
		Long productOptionId,
		Long productId,
		String productName,
		String optionName,
		Integer productPrice,
		Integer additionalPrice
	) {
		ProductOption productOption = mock(ProductOption.class);
		Product product = mock(Product.class);

		when(productOption.getId()).thenReturn(productOptionId);
		when(productOption.getOptionName()).thenReturn(optionName);
		when(productOption.getAdditionalPrice()).thenReturn(additionalPrice);
		when(productOption.getProduct()).thenReturn(product);

		when(product.getId()).thenReturn(productId);
		when(product.getName()).thenReturn(productName);
		when(product.getPrice()).thenReturn(productPrice);

		return productOption;
	}

	private Address createAddress(Member member) {
		return Address.of(
			member,
			"홍길동",
			"010-1234-5678",
			"12345",
			"서울시 강남구 테헤란로",
			"101동 1001호",
			true
		);
	}

	private Order createOrder(
		Long orderId,
		Member member,
		String orderNumber,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount
	) {
		Order order = Order.of(
			member,
			orderNumber,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount
		);

		ReflectionTestUtils.setField(order, "id", orderId);
		ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2026, 6, 26, 10, 0));

		return order;
	}

	private OrderItem createOrderItem(
		Long orderItemId,
		Order order,
		ProductOption productOption,
		Long cartItemId,
		Long productId,
		String productName,
		String optionName,
		Integer unitPrice,
		Integer quantity
	) {
		OrderItem orderItem = OrderItem.of(order, productOption, quantity);

		ReflectionTestUtils.setField(orderItem, "id", orderItemId);
		ReflectionTestUtils.setField(orderItem, "cartItemId", cartItemId);

		return orderItem;
	}

	private Payment createPayment(
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