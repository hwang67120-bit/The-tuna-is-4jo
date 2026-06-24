package com.example.thetunais4joteamproject.domain.user.dto;

import com.example.thetunais4joteamproject.domain.user.entity.Member;

public record LoginMemberResponse(
        Long memberId,
        String role
) {

    public static LoginMemberResponse from(Member member) {
        return new LoginMemberResponse(member.getId(), member.getRole());
    }
}
