package com.example.thetunais4joteamproject.domain.refund.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.thetunais4joteamproject.domain.refund.dto.AdminRefundResponse;
import com.example.thetunais4joteamproject.domain.refund.dto.RefundRejectRequest;
import com.example.thetunais4joteamproject.domain.refund.dto.RefundRequest;
import com.example.thetunais4joteamproject.domain.refund.dto.RefundResponse;
import com.example.thetunais4joteamproject.domain.refund.service.RefundService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RefundController {

	private final RefundService refundService;

	@PostMapping("/refunds")
	public ResponseEntity<RefundResponse> requestRefund(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody RefundRequest request
	) {
		return ResponseEntity.ok(refundService.requestRefund(memberId, request));
	}

	@GetMapping("/admin/refunds")
	public ResponseEntity<List<AdminRefundResponse>> getAdminRefunds() {
		return ResponseEntity.ok(refundService.getAdminRefunds());
	}

	@PostMapping("/admin/refunds/{refundId}/approve")
	public ResponseEntity<RefundResponse> approveRefund(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long refundId
	) {
		return ResponseEntity.ok(refundService.approveRefund(memberId, refundId));
	}

	@PostMapping("/admin/refunds/{refundId}/reject")
	public ResponseEntity<RefundResponse> rejectRefund(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long refundId,
		@Valid @RequestBody RefundRejectRequest request
	) {
		return ResponseEntity.ok(
			refundService.rejectRefund(memberId, refundId, request.rejectionReason())
		);
	}
}
