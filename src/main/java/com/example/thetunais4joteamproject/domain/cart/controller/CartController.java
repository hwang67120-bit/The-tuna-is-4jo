package com.example.thetunais4joteamproject.domain.cart.controller;

import com.example.thetunais4joteamproject.domain.cart.dto.CreateCartItemRequest;
import com.example.thetunais4joteamproject.domain.cart.dto.CreateCartItemResponse;
import com.example.thetunais4joteamproject.domain.cart.dto.GetCartResponse;
import com.example.thetunais4joteamproject.domain.cart.dto.UpdateCartItemRequest;
import com.example.thetunais4joteamproject.domain.cart.dto.UpdateCartItemResponse;
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

	@GetMapping
	public ResponseEntity<ApiResponse<GetCartResponse>> getCart(
		@AuthenticationPrincipal Long memberId
	) {
		GetCartResponse response = cartFacade.getCart(memberId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
	}

	@PatchMapping("/{cartItemId}")
	public ResponseEntity<ApiResponse<UpdateCartItemResponse>> updateCartItemQuantity(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long cartItemId,
		@Valid @RequestBody UpdateCartItemRequest request
	) {
		UpdateCartItemResponse response = cartFacade.updateCartItemQuantity(
			memberId,
			cartItemId,
			request
		);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
	}

	@DeleteMapping("/items")
	public ResponseEntity<ApiResponse<Void>> clearCart(
		@AuthenticationPrincipal Long memberId
	) {
		cartFacade.clearCart(memberId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("장바구니가 비워졌습니다."));
	}

	@DeleteMapping("/{cartItemId}")
	public ResponseEntity<ApiResponse<Void>> deleteCartItem(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long cartItemId
	) {
		cartFacade.deleteCartItem(memberId, cartItemId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("선택 상품이 삭제되었습니다."));
	}
}