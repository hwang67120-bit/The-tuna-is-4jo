package com.example.thetunais4joteamproject.domain.order.entity;

import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.global.common.BaseEntity;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private Integer deliveryPrice;

    @Column(nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    private Order(
        Member member,
        String orderNumber,
        Integer orderPrice,
        Integer discountPrice,
        Integer deliveryPrice,
        Integer totalAmount
    ) {
        this.member = member;
        this.orderNumber = orderNumber;
        this.orderPrice = orderPrice;
        this.discountPrice = discountPrice;
        this.deliveryPrice = deliveryPrice;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    public static Order of(
        Member member,
        String orderNumber,
        Integer orderPrice,
        Integer discountPrice,
        Integer deliveryPrice,
        Integer totalAmount
    ) {
        return new Order(member, orderNumber, orderPrice, discountPrice, deliveryPrice, totalAmount);
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
}