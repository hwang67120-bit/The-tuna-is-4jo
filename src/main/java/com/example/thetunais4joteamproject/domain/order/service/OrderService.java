package com.example.thetunais4joteamproject.domain.order.service;

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
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount
	) {
		Order order = Order.of(
			member,
			createOrderNumber(),
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount
		);

		return orderRepository.save(order);
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

	// 주문 취소 등 상태 변경 작업에서는 로그인 회원의 주문인지 먼저 확인합니다.
	public Order getOrder(Long memberId, Long orderId) {
		return orderRepository.findByIdAndMemberId(orderId, memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ORDER_NOT_FOUND));
	}

	// 결제 대기 주문은 주문 내역에 노출하지 않고, 확정된 주문만 최신순으로 조회합니다.
	public List<Order> getConfirmedOrders(Long memberId) {
		return orderRepository.findAllByMemberIdAndStatusOrderByCreatedAtDesc(
			memberId,
			OrderStatus.CONFIRMED
		);
	}

	// 다른 회원의 주문이거나 확정되지 않은 주문이면 조회되지 않도록 조건을 함께 검증합니다.
	public Order getConfirmedOrder(Long memberId, Long orderId) {
		return orderRepository.findByIdAndMemberIdAndStatus(
				orderId,
				memberId,
				OrderStatus.CONFIRMED
			)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ORDER_NOT_FOUND));
	}

	// 주문 상세와 취소 처리에서 사용할 주문 상품 목록을 조회합니다.
	public List<OrderItem> getOrderItems(Long orderId) {
		return orderItemRepository.findAllByOrderIdWithProductOption(orderId);
	}

	private String createOrderNumber() {
		return "ORD-" + UUID.randomUUID()
			.toString()
			.replace("-", "")
			.substring(0, 16);
	}
}