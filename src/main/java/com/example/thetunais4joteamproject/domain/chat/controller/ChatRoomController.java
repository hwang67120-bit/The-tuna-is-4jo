package com.example.thetunais4joteamproject.domain.chat.controller;

import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomRequest;
import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.CloseChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatMessageResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomListResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.JoinChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.service.ChatMessageService;
import com.example.thetunais4joteamproject.domain.chat.service.ChatRoomService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;

import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;
	private final ChatMessageService chatMessageService;

	@PostMapping
	public ResponseEntity<ApiResponse<CreateChatRoomResponse>> create(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody CreateChatRoomRequest createChatRoomRequest
	) {
		CreateChatRoomResponse createChatRoomResponse = chatRoomService.create(memberId, createChatRoomRequest);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(createChatRoomResponse));
	}

	@PostMapping("/{chatRoomId}/join")
	public ResponseEntity<ApiResponse<JoinChatRoomResponse>> join(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long chatRoomId
	) {
		JoinChatRoomResponse joinChatRoomResponse = chatRoomService.join(memberId, chatRoomId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(joinChatRoomResponse));
	}

	@PatchMapping("/{chatRoomId}/close")
	public ResponseEntity<ApiResponse<CloseChatRoomResponse>> close(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long chatRoomId
	) {
		CloseChatRoomResponse closeChatRoomResponse = chatRoomService.close(memberId, chatRoomId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(closeChatRoomResponse));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<GetChatRoomListResponse>> getAll(
		@AuthenticationPrincipal Long memberId
	) {
		GetChatRoomListResponse getChatRoomListResponse = chatRoomService.getAll(memberId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(getChatRoomListResponse));
	}

	@GetMapping("/{chatRoomId}/messages")
	public ResponseEntity<ApiResponse<List<GetChatMessageResponse>>> getMessagesAfter(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long chatRoomId,
		@RequestParam(defaultValue = "0") Long afterMessageId
	) {
		List<GetChatMessageResponse> getChatMessageResponses = chatMessageService.getMessagesAfter(
			memberId,
			chatRoomId,
			afterMessageId
		);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(getChatMessageResponses));
	}

	@GetMapping("/{chatRoomId}")
	public ResponseEntity<ApiResponse<GetChatRoomResponse>> getOne(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long chatRoomId
	) {
		GetChatRoomResponse getChatRoomResponse = chatRoomService.getOne(memberId, chatRoomId);

		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(getChatRoomResponse));
	}
}
