package com.example.thetunais4joteamproject.domain.user.dto;

import com.example.thetunais4joteamproject.domain.user.entity.Member;

public record UpdateMemberInfoResponse(
        Long memberId,
        String name,
        String phoneNumber
) {

    public static UpdateMemberInfoResponse from(Member member) {
        return new UpdateMemberInfoResponse(
                member.getId(),
                member.getName(),
                member.getPhoneNumber()
        );
    }
}
