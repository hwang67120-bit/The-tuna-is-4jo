package com.example.thetunais4joteamproject.domain.user.dto;

import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;

public record GetMemberInfoResponse(
        Long memberId,
        String email,
        String name,
        String phoneNumber,
        MemberRole role
) {

    public static GetMemberInfoResponse from(Member member) {
        return new GetMemberInfoResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNumber(),
                member.getRole()
        );
    }
}
