package com.example.thetunais4joteamproject.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateMemberInfoRequest(
        @NotBlank
        String name,

        @NotBlank
        @Pattern(regexp = "^(\\d{11}|\\d{3}-\\d{4}-\\d{4})$")
        String phoneNumber
) {
}
