package com.example.thetunais4joteamproject.domain.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;
import com.example.thetunais4joteamproject.domain.order.entity.OrderStatus;
import com.example.thetunais4joteamproject.domain.order.repository.OrderItemRepository;
import com.example.thetunais4joteamproject.domain.order.repository.OrderRepository;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	// 주문 생성 시점의 금액을 스냅샷으로 저장하고 결제 대기 상태 주문을 생성합니다.
	public Order createOrder(
		Member member,
		Long memberCouponId,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount
	) {
		Order order = Order.of(
			member,
			createOrderNumber(),
			memberCouponId,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount
		);

		return orderRepository.save(order);
	}

	public Order createOrder(
		Member member,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount
	) {
		return createOrder(
			member,
			null,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount
		);
	}

	// 바로 주문 상품은 상품 옵션과 요청 수량을 기준으로 주문 상품을 생성합니다.
	public List<OrderItem> createOrderItems(
		Order order,
		List<ProductOption> productOptions,
		List<Integer> quantities
	) {
		List<OrderItem> orderItems = new ArrayList<>();

		for (int i = 0; i < productOptions.size(); i++) {
			OrderItem orderItem = OrderItem.of(
				order,
				productOptions.get(i),
				quantities.get(i)
			);

			orderItems.add(orderItemRepository.save(orderItem));
		}

		return orderItems;
	}

	// 장바구니 주문 상품은 결제 완료 후 삭제 대상을 알 수 있도록 cartItemId를 함께 저장합니다.
	public List<OrderItem> createOrderItemsFromCartItems(
		Order order,
		List<CartItem> cartItems
	) {
		List<OrderItem> orderItems = new ArrayList<>();

		for (CartItem cartItem : cartItems) {
			OrderItem orderItem = OrderItem.fromCartItem(order, cartItem);

			orderItems.add(orderItemRepository.save(orderItem));
		}

		return orderItems;
	}

	// 같은 장바구니 상품으로 결제 대기 주문이 이미 있으면 중복 주문 생성을 막습니다.
	public void validateNoPendingCartOrder(Long memberId, List<CartItem> cartItems) {
		List<Long> cartItemIds = getCartItemIdsFromCartItems(cartItems);
		boolean existsPendingOrder = orderItemRepository.existsByCartItemIdsAndMemberIdAndOrderStatus(
			cartItemIds,
			memberId,
			OrderStatus.PENDING_PAYMENT
		);

		if (existsPendingOrder) {
			throw BusinessException.from(ErrorCode.ORDER_ALREADY_PENDING);
		}
	}

	// 주문 취소 등 상태 변경 작업에서는 로그인 회원의 주문인지 먼저 확인합니다.
	public Order getOrder(Long memberId, Long orderId) {
		return orderRepository.findByIdAndMemberId(orderId, memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ORDER_NOT_FOUND));
	}

	// 주문 내역에서는 결제 대기, 결제 완료, 취소 주문을 모두 최신순으로 조회합니다.
	public List<Order> getOrders(Long memberId) {
		return orderRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
	}

	// 결제 대기 시간이 지난 주문을 만료 처리 대상으로 조회합니다.
	public List<Order> getExpiredPendingOrders(LocalDateTime expiredAt) {
		return orderRepository.findExpiredPendingOrders(
			OrderStatus.PENDING_PAYMENT,
			expiredAt
		);
	}

	// 주문 상세는 상태와 관계없이 로그인 회원의 주문인지 검증한 뒤 조회합니다.
	public Order getOrderDetail(Long memberId, Long orderId) {
		return orderRepository.findByIdAndMemberId(orderId, memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ORDER_NOT_FOUND));
	}

	// 주문 상세와 취소 처리에서 사용할 주문 상품 목록을 조회합니다.
	public List<OrderItem> getOrderItems(Long orderId) {
		return orderItemRepository.findAllByOrderIdWithProductOption(orderId);
	}

	// 장바구니 주문에서 생성된 주문 상품만 장바구니 삭제 대상으로 추출합니다.
	public List<Long> getCartItemIds(List<OrderItem> orderItems) {
		List<Long> cartItemIds = new ArrayList<>();

		for (OrderItem orderItem : orderItems) {
			if (orderItem.getCartItemId() != null) {
				cartItemIds.add(orderItem.getCartItemId());
			}
		}

		return cartItemIds;
	}

	private List<Long> getCartItemIdsFromCartItems(List<CartItem> cartItems) {
		List<Long> cartItemIds = new ArrayList<>();

		for (CartItem cartItem : cartItems) {
			cartItemIds.add(cartItem.getId());
		}

		return cartItemIds;
	}

	private String createOrderNumber() {
		return "ORD-" + UUID.randomUUID()
			.toString()
			.replace("-", "")
			.substring(0, 16);
	}
}