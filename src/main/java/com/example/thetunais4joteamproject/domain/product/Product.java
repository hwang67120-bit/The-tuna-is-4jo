package com.example.thetunais4joteamproject.domain.product;

import com.example.thetunais4joteamproject.global.common.BaseEntity;

public class Product extends BaseEntity {

    private Long id;
    private Long categoryId;
    private String name;
    private Integer price;
    private String description;
    private Integer stock;
    private String status;
}
