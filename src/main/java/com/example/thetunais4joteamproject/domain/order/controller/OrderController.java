package com.example.thetunais4joteamproject.domain.order.controller;

import java.util.List;

import com.example.thetunais4joteamproject.domain.order.dto.CreateCartOrderRequest;
import com.example.thetunais4joteamproject.domain.order.dto.CreateDirectOrderRequest;
import com.example.thetunais4joteamproject.domain.order.dto.CreateOrderResponse;
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewResponse;
import com.example.thetunais4joteamproject.domain.order.facade.OrderFacade;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderFacade orderFacade;

	@GetMapping("/preview")
	public ResponseEntity<ApiResponse<OrderPreviewResponse>> previewOrder(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(required = false) List<Long> cartItemIds
	) {
		OrderPreviewResponse response = orderFacade.previewOrder(memberId, cartItemIds);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
	}

	@PostMapping("/cart")
	public ResponseEntity<ApiResponse<CreateOrderResponse>> createCartOrder(
		@AuthenticationPrincipal Long memberId,
		@RequestBody(required = false) CreateCartOrderRequest request
	) {
		CreateOrderResponse response = orderFacade.createCartOrder(memberId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
	}

	@PostMapping("/direct")
	public ResponseEntity<ApiResponse<CreateOrderResponse>> createDirectOrder(
		@AuthenticationPrincipal Long memberId,
		@RequestBody CreateDirectOrderRequest request
	) {
		CreateOrderResponse response = orderFacade.createDirectOrder(memberId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
	}
}