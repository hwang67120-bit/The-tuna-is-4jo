package com.example.thetunais4joteamproject.domain.chat.controller;

import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomRequest;
import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.service.ChatRoomService;
import com.example.thetunais4joteamproject.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateChatRoomResponse>> create(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CreateChatRoomRequest createChatRoomRequest
    ) {
        CreateChatRoomResponse createChatRoomResponse = chatRoomService.create(memberId, createChatRoomRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(createChatRoomResponse));
    }
}
