package com.example.thetunais4joteamproject.domain.cart;

import com.example.thetunais4joteamproject.global.common.BaseEntity;

public class CartItem extends BaseEntity {

    private Long id;
    private Long cartId;
    private Long productId;
    private Integer quantity;
}
