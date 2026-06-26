package com.example.thetunais4joteamproject.domain.order.entity;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.global.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    @Column
    private Long cartItemId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String optionName;

    @Column(nullable = false)
    private Integer unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer totalPrice;

    private OrderItem(
        Order order,
        ProductOption productOption,
        Long cartItemId,
        Long productId,
        String productName,
        String optionName,
        Integer unitPrice,
        Integer quantity
    ) {
        this.order = order;
        this.productOption = productOption;
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.optionName = optionName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = unitPrice * quantity;
    }

    public static OrderItem fromCartItem(Order order, CartItem cartItem) {
        ProductOption productOption = cartItem.getProductOption();
        Product product = productOption.getProduct();
        int unitPrice = product.getPrice() + productOption.getAdditionalPrice();

        // 결제 완료 후 주문에 포함된 장바구니 상품만 삭제하기 위해 cartItemId를 스냅샷으로 저장합니다.
        return new OrderItem(
            order,
            productOption,
            cartItem.getId(),
            product.getId(),
            product.getName(),
            productOption.getOptionName(),
            unitPrice,
            cartItem.getQuantity()
        );
    }

    public static OrderItem of(Order order, ProductOption productOption, Integer quantity) {
        Product product = productOption.getProduct();
        int unitPrice = product.getPrice() + productOption.getAdditionalPrice();

        // 바로 주문은 장바구니 상품을 거치지 않으므로 cartItemId를 저장하지 않습니다.
        return new OrderItem(
            order,
            productOption,
            null,
            product.getId(),
            product.getName(),
            productOption.getOptionName(),
            unitPrice,
            quantity
        );
    }
}
