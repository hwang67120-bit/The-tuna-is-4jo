package com.example.thetunais4joteamproject.domain.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.order.entity.DeliveryAddress;
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

	public Order createOrder(
		Member member,
		Long memberCouponId,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount,
		DeliveryAddress deliveryAddress
	) {
		Order order = Order.of(
			member,
			createOrderNumber(),
			memberCouponId,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount,
			deliveryAddress
		);

		return orderRepository.save(order);
	}

	public Order createOrder(
		Member member,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount,
		DeliveryAddress deliveryAddress
	) {
		return createOrder(
			member,
			null,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount,
			deliveryAddress
		);
	}

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

	public Order getOrder(Long memberId, Long orderId) {
		return orderRepository.findByIdAndMemberId(orderId, memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ORDER_NOT_FOUND));
	}

	public List<Order> getOrders(Long memberId) {
		return orderRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
	}

	public List<Order> getExpiredPendingOrders(LocalDateTime expiredAt) {
		return orderRepository.findExpiredPendingOrders(
			OrderStatus.PENDING_PAYMENT,
			expiredAt
		);
	}

	public Order getOrderDetail(Long memberId, Long orderId) {
		return orderRepository.findByIdAndMemberId(orderId, memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ORDER_NOT_FOUND));
	}

	public List<OrderItem> getOrderItems(Long orderId) {
		return orderItemRepository.findAllByOrderIdWithProductOption(orderId);
	}

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
