package com.example.thetunais4joteamproject.domain.order;

import com.example.thetunais4joteamproject.global.common.BaseEntity;

public class OrderItem extends BaseEntity {

    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer originalPrice;
    private Integer salePrice;
    private Integer quantity;
    private Integer totalAmount;
}
