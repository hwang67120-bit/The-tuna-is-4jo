package com.example.thetunais4joteamproject.domain.user.dto;

import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;

public record LoginMemberResponse(
        Long memberId,
        MemberRole role,
        String accessToken
) {

    public static LoginMemberResponse from(Member member, String accessToken) {
        return new LoginMemberResponse(member.getId(), member.getRole(), accessToken);
    }
}
