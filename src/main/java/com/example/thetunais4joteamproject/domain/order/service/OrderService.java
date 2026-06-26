package com.example.thetunais4joteamproject.domain.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;
import com.example.thetunais4joteamproject.domain.order.repository.OrderItemRepository;
import com.example.thetunais4joteamproject.domain.order.repository.OrderRepository;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.user.entity.Member;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

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

	private String createOrderNumber() {
		return "ORD-" + UUID.randomUUID()
			.toString()
			.replace("-", "")
			.substring(0, 16);
	}
}