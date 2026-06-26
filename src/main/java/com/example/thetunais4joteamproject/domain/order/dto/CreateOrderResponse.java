package com.example.thetunais4joteamproject.domain.order.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;
import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

public record CreateOrderResponse(
	Long orderId,
	String orderNumber,
	Long paymentId,
	String portonePaymentId,
	Integer orderPrice,
	Integer discountPrice,
	Integer deliveryPrice,
	Integer totalAmount,
	String orderStatus,
	String paymentStatus,
	List<CreateOrderItemResponse> items
) {

	public static CreateOrderResponse of(
		Order order,
		Payment payment,
		List<OrderItem> orderItems
	) {
		List<CreateOrderItemResponse> items = new ArrayList<>();

		for (OrderItem orderItem : orderItems) {
			items.add(CreateOrderItemResponse.from(orderItem));
		}

		return new CreateOrderResponse(
			order.getId(),
			order.getOrderNumber(),
			payment.getId(),
			payment.getPortonePaymentId(),
			order.getOrderPrice(),
			order.getDiscountPrice(),
			order.getDeliveryPrice(),
			order.getTotalAmount(),
			order.getStatus().name(),
			payment.getStatus().name(),
			items
		);
	}
}