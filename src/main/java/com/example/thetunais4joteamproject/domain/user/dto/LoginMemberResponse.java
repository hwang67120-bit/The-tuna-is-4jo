package com.example.thetunais4joteamproject.domain.user.dto;

import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;

public record LoginMemberResponse(
        Long memberId,
        String email,
        String name,
        String phoneNumber,
        MemberRole role,
        String accessToken
) {

    public static LoginMemberResponse from(Member member, String accessToken) {
        return new LoginMemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNumber(),
                member.getRole(),
                accessToken
        );
    }
}
