package com.example.thetunais4joteamproject.domain.user.dto;

import com.example.thetunais4joteamproject.domain.user.entity.Member;

public record CreateMemberResponse(
	Long memberId
) {

	public static CreateMemberResponse from(Member savedMember) {
		return new CreateMemberResponse(savedMember.getId());
	}
}