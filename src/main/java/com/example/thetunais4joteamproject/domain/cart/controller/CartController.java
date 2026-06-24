package com.example.thetunais4joteamproject.domain.cart.controller;

import com.example.thetunais4joteamproject.domain.cart.dto.CreateCartItemRequest;
import com.example.thetunais4joteamproject.domain.cart.dto.CreateCartItemResponse;
import com.example.thetunais4joteamproject.domain.cart.facade.CartFacade;
import com.example.thetunais4joteamproject.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

	private final CartFacade cartFacade;

	@PostMapping("/items")
	public ResponseEntity<ApiResponse<CreateCartItemResponse>> createCartItem(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody CreateCartItemRequest request
	) {
		CreateCartItemResponse response = cartFacade.createCartItem(memberId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
	}
}