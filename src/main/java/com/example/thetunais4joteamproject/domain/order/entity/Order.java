package com.example.thetunais4joteamproject.domain.order.entity;

import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, unique = true, length = 50)
	private String orderNumber;

	@Column(nullable = false)
	private Integer orderPrice;

	@Column(nullable = false)
	private Integer discountPrice;

	private Long memberCouponId;

	@Column(nullable = false)
	private Integer deliveryPrice;

	@Column(nullable = false)
	private Integer totalAmount;

	@Embedded
	private DeliveryAddress deliveryAddress;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OrderStatus status;

	private Order(
		Member member,
		String orderNumber,
		Long memberCouponId,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount,
		DeliveryAddress deliveryAddress
	) {
		this.member = member;
		this.orderNumber = orderNumber;
		this.memberCouponId = memberCouponId;
		this.orderPrice = orderPrice;
		this.discountPrice = discountPrice;
		this.deliveryPrice = deliveryPrice;
		this.totalAmount = totalAmount;
		this.deliveryAddress = deliveryAddress;
		this.status = OrderStatus.PENDING_PAYMENT;
	}

	public static Order of(
		Member member,
		String orderNumber,
		Long memberCouponId,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount,
		DeliveryAddress deliveryAddress
	) {
		return new Order(
			member,
			orderNumber,
			memberCouponId,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount,
			deliveryAddress
		);
	}

	public static Order of(
		Member member,
		String orderNumber,
		Long memberCouponId,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount
	) {
		return new Order(
			member,
			orderNumber,
			memberCouponId,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount,
			null
		);
	}

	public static Order of(
		Member member,
		String orderNumber,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount,
		DeliveryAddress deliveryAddress
	) {
		return new Order(
			member,
			orderNumber,
			null,
			orderPrice,
			discountPrice,
			deliveryPrice,
			totalAmount,
			deliveryAddress
		);
	}

	public static Order of(
		Member member,
		String orderNumber,
		Integer orderPrice,
		Integer discountPrice,
		Integer deliveryPrice,
		Integer totalAmount
	) {
		return new Order(member, orderNumber, null, orderPrice, discountPrice, deliveryPrice, totalAmount, null);
	}

	public void changeStatus(OrderStatus status) {
		this.status.validateTransition(status);
		this.status = status;
	}

	public void confirm() {
		changeStatus(OrderStatus.CONFIRMED);
	}

	public void cancel() {
		changeStatus(OrderStatus.CANCELED);
	}

	public void expire() {
		changeStatus(OrderStatus.EXPIRED);
	}
}
