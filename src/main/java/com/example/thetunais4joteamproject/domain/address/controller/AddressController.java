package com.example.thetunais4joteamproject.domain.address.controller;

import java.util.List;

import com.example.thetunais4joteamproject.domain.address.dto.CreateAddressRequest;
import com.example.thetunais4joteamproject.domain.address.dto.AddressResponse;
import com.example.thetunais4joteamproject.domain.address.dto.UpdateAddressRequest;
import com.example.thetunais4joteamproject.domain.address.service.AddressService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {

	private final AddressService addressService;

	@PostMapping
	public ResponseEntity<ApiResponse<AddressResponse>> create(
		@AuthenticationPrincipal Long memberId,
		@RequestBody CreateAddressRequest request
	) {
		AddressResponse response = addressService.create(memberId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<AddressResponse>>> getAll(
		@AuthenticationPrincipal Long memberId
	) {
		List<AddressResponse> response = addressService.getAll(memberId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
	}

	@PatchMapping("/{addressId}")
	public ResponseEntity<ApiResponse<AddressResponse>> update(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long addressId,
		@RequestBody UpdateAddressRequest request
	) {
		AddressResponse response = addressService.update(memberId, addressId, request);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
	}

	@PatchMapping("/{addressId}/default")
	public ResponseEntity<ApiResponse<AddressResponse>> changeDefault(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long addressId
	) {
		AddressResponse response = addressService.changeDefault(memberId, addressId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
	}

	@DeleteMapping("/{addressId}")
	public ResponseEntity<ApiResponse<Void>> delete(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long addressId
	) {
		addressService.delete(memberId, addressId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("배송지 정보가 삭제되었습니다."));
	}
}