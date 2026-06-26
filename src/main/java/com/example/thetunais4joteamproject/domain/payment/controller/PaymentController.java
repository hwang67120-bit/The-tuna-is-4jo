package com.example.thetunais4joteamproject.domain.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmRequest;
import com.example.thetunais4joteamproject.domain.payment.dto.PaymentConfirmResponse;
import com.example.thetunais4joteamproject.domain.payment.facade.PaymentFacade;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentFacade paymentFacade;

	@PostMapping("/confirm")
	public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody PaymentConfirmRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(paymentFacade.confirmPayment(memberId, request)));
	}

}
