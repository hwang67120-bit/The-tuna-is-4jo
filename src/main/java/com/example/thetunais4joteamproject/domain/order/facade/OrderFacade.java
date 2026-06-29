package com.example.thetunais4joteamproject.domain.order.facade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewItemResponse;
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewResponse;
import com.example.thetunais4joteamproject.domain.order.entity.DeliveryAddress;
import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;
import com.example.thetunais4joteamproject.domain.order.service.OrderService;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentCommandService;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.product.repository.ProductOptionRepository;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacade {

	private static final int DEFAULT_DELIVERY_PRICE = 3000;
	private static final int PAYMENT_PENDING_EXPIRATION_MINUTES = 10;

	private final CartService cartService;
	private final OrderService orderService;
	private final PaymentCommandService paymentCommandService;
	private final MemberRepository memberRepository;
	private final ProductOptionRepository productOptionRepository;
	private final CouponService couponService;
	private final AddressRepository addressRepository;

	@Transactional(readOnly = true)
	public OrderPreviewResponse previewOrder(Long memberId, List<Long> cartItemIds) {
		return previewOrder(memberId, cartItemIds, null);
	}

	@Transactional(readOnly = true)
	public OrderPreviewResponse previewOrder(Long memberId, List<Long> cartItemIds, Long memberCouponId) {
		validateMemberExists(memberId);

		List<Long> previewCartItemIds = getCartItemIds(cartItemIds);
		List<CartItem> cartItems = cartService.getPreviewItems(memberId, previewCartItemIds);
		List<OrderPreviewItemResponse> items = getOrderPreviewItems(cartItems);
		int orderPrice = calculateCartOrderPrice(cartItems);
		int discountPrice = couponService.calculateDiscountPrice(memberId, memberCouponId, orderPrice);

		return OrderPreviewResponse.of(items, discountPrice, DEFAULT_DELIVERY_PRICE);
	}

	@Transactional
	public CreateOrderResponse createCartOrder(Long memberId, CreateCartOrderRequest request) {
		Member member = getMember(memberId);
		List<Long> cartItemIds = getCartItemIds(request);
		List<CartItem> cartItems = cartService.getPreviewItems(memberId, cartItemIds);

		orderService.validateNoPendingCartOrder(memberId, cartItems);
		DeliveryAddress deliveryAddress = getDeliveryAddress(memberId, getMemberAddressId(request));
		decreaseCartItemStock(cartItems);

		int orderPrice = calculateCartOrderPrice(cartItems);
		Long memberCouponId = getMemberCouponId(request);
		int discountPrice = couponService.calculateDiscountPrice(memberId, memberCouponId, orderPrice);
		int totalAmount = orderPrice - discountPrice + DEFAULT_DELIVERY_PRICE;

		Order order = createOrder(
			member,
			memberCouponId,
			orderPrice,
			discountPrice,
			DEFAULT_DELIVERY_PRICE,
			totalAmount,
			deliveryAddress
		);

		List<OrderItem> orderItems = orderService.createOrderItemsFromCartItems(order, cartItems);
		Payment payment = paymentCommandService.createPayment(order);

		return CreateOrderResponse.of(order, payment, orderItems);
	}

	@Transactional
	public CreateOrderResponse createDirectOrder(Long memberId, CreateDirectOrderRequest request) {
		Member member = getMember(memberId);

		validateDirectOrderRequest(request);

		ProductOption productOption = productOptionRepository.findById(request.productOptionId())
			.orElseThrow(() -> BusinessException.from(ErrorCode.OPTION_NOT_FOUND));

		List<ProductOption> productOptions = List.of(productOption);
		List<Integer> quantities = List.of(request.quantity());
		DeliveryAddress deliveryAddress = getDeliveryAddress(memberId, request.memberAddressId());

		return createOrderAndPayment(
			member,
			request.memberCouponId(),
			deliveryAddress,
			productOptions,
			quantities
		);
	}

	@Transactional
	public CancelOrderResponse cancelOrder(Long memberId, Long orderId) {
		Order order = orderService.getOrder(memberId, orderId);

		order.cancel();

		List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
		restoreOrderItemStock(orderItems);

		Payment payment = paymentCommandService.cancelPayment(order);

		return CancelOrderResponse.of(order, payment);
	}

	@Transactional
	public void expirePendingOrders() {
		LocalDateTime expiredAt = LocalDateTime.now()
			.minusMinutes(PAYMENT_PENDING_EXPIRATION_MINUTES);
		List<Order> orders = orderService.getExpiredPendingOrders(expiredAt);

		for (Order order : orders) {
			expirePendingOrder(order);
		}
	}

	@Transactional(readOnly = true)
	public List<GetOrderResponse> getAll(Long memberId) {
		validateMemberExists(memberId);

		List<Order> orders = orderService.getOrders(memberId);
		List<GetOrderResponse> responses = new ArrayList<>();

		for (Order order : orders) {
			responses.add(GetOrderResponse.from(order));
		}

		return responses;
	}

	@Transactional(readOnly = true)
	public GetOrderDetailResponse getOne(Long memberId, Long orderId) {
		validateMemberExists(memberId);

		Order order = orderService.getOrderDetail(memberId, orderId);
		List<OrderItem> orderItems = orderService.getOrderItems(order.getId());

		return GetOrderDetailResponse.of(order, orderItems);
	}

	private CreateOrderResponse createOrderAndPayment(
		Member member,
		Long memberCouponId,
		DeliveryAddress deliveryAddress,
		List<ProductOption> productOptions,
		List<Integer> quantities
	) {
		decreaseProductOptionStock(productOptions, quantities);

		int orderPrice = calculateOrderPrice(productOptions, quantities);
		int discountPrice = couponService.calculateDiscountPrice(member.getId(), memberCouponId, orderPrice);
		int totalAmount = orderPrice - discountPrice + DEFAULT_DELIVERY_PRICE;

		Order order = createOrder(
			member,
			memberCouponId,
			orderPrice,
			discountPrice,
			DEFAULT_DELIVERY_PRICE,
			totalAmount,
			deliveryAddress
		);

		List<OrderItem> orderItems = orderService.createOrderItems(order, productOptions, quantities);
		Payment payment = paymentCommandService.createPayment(order);

		return CreateOrderResponse.of(order, payment, orderItems);
	}

	private Order createOrder(
		Member member,
		Long memberCouponId,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount,
		DeliveryAddress deliveryAddress
	) {
		if (memberCouponId == null) {
			return orderService.createOrder(
				member,
				orderPrice,
				discountPrice,
				deliveryPrice,
				totalAmount,
				deliveryAddress
			);
		}

		return orderService.createOrder(
			member,
			memberCouponId,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount,
			deliveryAddress
		);
	}

	private DeliveryAddress getDeliveryAddress(Long memberId, Long memberAddressId) {
		Address address = memberAddressId != null
			? addressRepository.findByIdAndMemberId(memberAddressId, memberId)
				.orElseThrow(() -> BusinessException.from(ErrorCode.ADDRESS_NOT_FOUND))
			: addressRepository.findFirstByMemberIdAndDefaultAddressTrue(memberId)
				.orElseThrow(() -> BusinessException.from(ErrorCode.ADDRESS_NOT_FOUND));

		return DeliveryAddress.from(address);
	}

	private void expirePendingOrder(Order order) {
		order.expire();

		List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
		restoreOrderItemStock(orderItems);

		paymentCommandService.expirePayment(order);
	}

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));
	}

	private void validateMemberExists(Long memberId) {
		memberRepository.findById(memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));
	}

	private void validateDirectOrderRequest(CreateDirectOrderRequest request) {
		if (request == null || request.productOptionId() == null) {
			throw BusinessException.from(ErrorCode.BAD_REQUEST);
		}

		validateOrderQuantity(request.quantity());
	}

	private void validateOrderQuantity(Integer quantity) {
		if (quantity == null || quantity < 1) {
			throw BusinessException.from(ErrorCode.INVALID_ORDER_QUANTITY);
		}
	}

	private void decreaseCartItemStock(List<CartItem> cartItems) {
		for (CartItem cartItem : cartItems) {
			cartItem.getProductOption().decreaseStock(cartItem.getQuantity());
		}
	}

	private void decreaseProductOptionStock(
		List<ProductOption> productOptions,
		List<Integer> quantities
	) {
		for (int i = 0; i < productOptions.size(); i++) {
			productOptions.get(i).decreaseStock(quantities.get(i));
		}
	}

	private void restoreOrderItemStock(List<OrderItem> orderItems) {
		for (OrderItem orderItem : orderItems) {
			orderItem.getProductOption().increaseStock(orderItem.getQuantity());
		}
	}

	private int calculateCartOrderPrice(List<CartItem> cartItems) {
		int orderPrice = 0;

		for (CartItem cartItem : cartItems) {
			ProductOption productOption = cartItem.getProductOption();
			int unitPrice = productOption.getProduct().getPrice() + productOption.getAdditionalPrice();

			orderPrice += unitPrice * cartItem.getQuantity();
		}

		return orderPrice;
	}

	private int calculateOrderPrice(
		List<ProductOption> productOptions,
		List<Integer> quantities
	) {
		int orderPrice = 0;

		for (int i = 0; i < productOptions.size(); i++) {
			ProductOption productOption = productOptions.get(i);
			int unitPrice = productOption.getProduct().getPrice() + productOption.getAdditionalPrice();

			orderPrice += unitPrice * quantities.get(i);
		}

		return orderPrice;
	}

	private List<OrderPreviewItemResponse> getOrderPreviewItems(List<CartItem> cartItems) {
		List<OrderPreviewItemResponse> items = new ArrayList<>();

		for (CartItem cartItem : cartItems) {
			cartItem.getProductOption().validateEnoughStock(cartItem.getQuantity());
			items.add(OrderPreviewItemResponse.from(cartItem));
		}

		return items;
	}

	private List<Long> getCartItemIds(List<Long> cartItemIds) {
		return cartItemIds != null ? cartItemIds : List.of();
	}

	private List<Long> getCartItemIds(CreateCartOrderRequest request) {
		return request != null && request.cartItemIds() != null
			? request.cartItemIds()
			: List.of();
	}

	private Long getMemberCouponId(CreateCartOrderRequest request) {
		return request == null ? null : request.memberCouponId();
	}

	private Long getMemberAddressId(CreateCartOrderRequest request) {
		return request == null ? null : request.memberAddressId();
	}
}
