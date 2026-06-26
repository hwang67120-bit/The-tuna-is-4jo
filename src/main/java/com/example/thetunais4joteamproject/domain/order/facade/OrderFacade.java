package com.example.thetunais4joteamproject.domain.order.facade;

import java.util.ArrayList;
import java.util.List;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.cart.service.CartService;
import com.example.thetunais4joteamproject.domain.order.dto.CancelOrderResponse;
import com.example.thetunais4joteamproject.domain.order.dto.CreateCartOrderRequest;
import com.example.thetunais4joteamproject.domain.order.dto.CreateDirectOrderRequest;
import com.example.thetunais4joteamproject.domain.order.dto.CreateOrderResponse;
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewItemResponse;
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewResponse;
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

	private static final int DEFAULT_DISCOUNT_PRICE = 0;
	private static final int DEFAULT_DELIVERY_PRICE = 3000;

	private final CartService cartService;
	private final OrderService orderService;
	private final PaymentCommandService paymentCommandService;
	private final MemberRepository memberRepository;
	private final ProductOptionRepository productOptionRepository;

	@Transactional(readOnly = true)
	public OrderPreviewResponse previewOrder(Long memberId, List<Long> cartItemIds) {
		validateMemberExists(memberId);

		List<Long> previewCartItemIds = getCartItemIds(cartItemIds);
		List<CartItem> cartItems = cartService.getPreviewItems(memberId, previewCartItemIds);
		List<OrderPreviewItemResponse> items = getOrderPreviewItems(cartItems);

		// 쿠폰 도메인이 아직 구현되지 않았으므로 할인 금액은 0원, 배송비는 기본 정책인 3000원을 적용합니다.
		return OrderPreviewResponse.of(items, DEFAULT_DISCOUNT_PRICE, DEFAULT_DELIVERY_PRICE);
	}

	@Transactional
	public CreateOrderResponse createCartOrder(Long memberId, CreateCartOrderRequest request) {
		Member member = getMember(memberId);
		List<Long> cartItemIds = getCartItemIds(request);
		List<CartItem> cartItems = cartService.getPreviewItems(memberId, cartItemIds);

		decreaseCartItemStock(cartItems);

		int orderPrice = calculateCartOrderPrice(cartItems);
		int totalAmount = orderPrice - DEFAULT_DISCOUNT_PRICE + DEFAULT_DELIVERY_PRICE;

		Order order = orderService.createOrder(
			member,
			orderPrice,
			DEFAULT_DISCOUNT_PRICE,
			DEFAULT_DELIVERY_PRICE,
			totalAmount
		);

		List<OrderItem> orderItems = orderService.createOrderItemsFromCartItems(order, cartItems);
		Payment payment = paymentCommandService.createPayment(order);

		// 결제 확정 전에는 장바구니 상품을 삭제하지 않고, 결제 완료 처리 시점에 삭제합니다.
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

		return createOrderAndPayment(member, productOptions, quantities);
	}

	@Transactional
	public CancelOrderResponse cancelOrder(Long memberId, Long orderId) {
		Order order = orderService.getOrder(memberId, orderId);
		List<OrderItem> orderItems = orderService.getOrderItems(order.getId());

		restoreOrderItemStock(orderItems);

		order.cancel();
		Payment payment = paymentCommandService.cancelPayment(order);

		return CancelOrderResponse.of(order, payment);
	}

	private CreateOrderResponse createOrderAndPayment(
		Member member,
		List<ProductOption> productOptions,
		List<Integer> quantities
	) {
		decreaseProductOptionStock(productOptions, quantities);

		int orderPrice = calculateOrderPrice(productOptions, quantities);
		int totalAmount = orderPrice - DEFAULT_DISCOUNT_PRICE + DEFAULT_DELIVERY_PRICE;

		Order order = orderService.createOrder(
			member,
			orderPrice,
			DEFAULT_DISCOUNT_PRICE,
			DEFAULT_DELIVERY_PRICE,
			totalAmount
		);

		List<OrderItem> orderItems = orderService.createOrderItems(order, productOptions, quantities);
		Payment payment = paymentCommandService.createPayment(order);

		return CreateOrderResponse.of(order, payment, orderItems);
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
			// 주문 생성 시점에 재고를 먼저 차감하고, 이후 예외가 발생하면 트랜잭션으로 함께 롤백됩니다.
			cartItem.getProductOption().decreaseStock(cartItem.getQuantity());
		}
	}

	private void decreaseProductOptionStock(
		List<ProductOption> productOptions,
		List<Integer> quantities
	) {
		for (int i = 0; i < productOptions.size(); i++) {
			// 바로 주문은 장바구니 수량이 없으므로 요청 수량을 기준으로 재고를 차감합니다.
			productOptions.get(i).decreaseStock(quantities.get(i));
		}
	}

	private void restoreOrderItemStock(List<OrderItem> orderItems) {
		for (OrderItem orderItem : orderItems) {
			// 주문 생성 시 차감했던 재고를 주문 취소 시점에 다시 원복합니다.
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
}
