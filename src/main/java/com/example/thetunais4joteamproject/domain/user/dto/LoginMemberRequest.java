package com.example.thetunais4joteamproject.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginMemberRequest(
        @NotBlank
        @Email
        @Pattern(regexp = "^[A-Za-z0-9]+@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)+$")
        String email,

        @NotBlank
        String password
) {
}
