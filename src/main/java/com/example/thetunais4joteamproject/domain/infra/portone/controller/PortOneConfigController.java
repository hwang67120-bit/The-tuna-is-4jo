package com.example.thetunais4joteamproject.domain.infra.portone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.thetunais4joteamproject.domain.infra.portone.config.PortOneProperties;
import com.example.thetunais4joteamproject.domain.infra.portone.dto.PortOneConfigResponse;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PortOneConfigController {

	private final PortOneProperties portOneProperties;

	@GetMapping("/api/config/portone")
	public ResponseEntity<ApiResponse<PortOneConfigResponse>> getConfig() {
		return ResponseEntity.ok(ApiResponse.ok(new PortOneConfigResponse(
			portOneProperties.getStoreId(),
			portOneProperties.getChannelKey()
		)));
	}
}
