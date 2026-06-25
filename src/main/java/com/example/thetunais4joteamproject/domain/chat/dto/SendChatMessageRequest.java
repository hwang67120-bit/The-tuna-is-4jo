package com.example.thetunais4joteamproject.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendChatMessageRequest(
        @NotNull
        Long chatRoomId,

        @NotNull
        Long senderId,

        @NotBlank
        String content
) {
}
