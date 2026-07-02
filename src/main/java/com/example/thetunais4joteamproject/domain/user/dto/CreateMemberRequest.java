package com.example.thetunais4joteamproject.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateMemberRequest(
	@NotBlank
	@Email
	@Pattern(regexp = "^[A-Za-z0-9]+@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)+$")
	String email,

	@NotBlank
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$")
	String password,

	@NotBlank
	String name,

	@NotBlank
	@Pattern(regexp = "^(\\d{11}|\\d{3}-\\d{4}-\\d{4})$")
	String phoneNumber,

	String nickname
) {
}