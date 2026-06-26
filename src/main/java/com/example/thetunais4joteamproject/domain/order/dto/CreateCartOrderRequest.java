package com.example.thetunais4joteamproject.domain.order.dto;

import java.util.List;

public record CreateCartOrderRequest(
	List<Long> cartItemIds
) {
}