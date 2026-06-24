package com.example.thetunais4joteamproject.domain.user.dto;

public record GetMemberEmailCheckResponse(
        boolean available
) {

    public static GetMemberEmailCheckResponse from(boolean available) {
        return new GetMemberEmailCheckResponse(available);
    }
}