package com.example.thetunais4joteamproject.domain.order.dto;

public record CreateDirectOrderRequest(
	Long productOptionId,
	Integer quantity
) {
}