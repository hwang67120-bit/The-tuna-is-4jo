package com.example.thetunais4joteamproject.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChatRoomRequest(
	@NotBlank
	@Size(max = 100)
	String title,

	@NotBlank
	String content
) {
}
