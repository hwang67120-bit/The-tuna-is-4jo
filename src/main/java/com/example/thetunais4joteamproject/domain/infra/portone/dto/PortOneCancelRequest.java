package com.example.thetunais4joteamproject.domain.infra.portone.dto;

public record PortOneCancelRequest(
	String reason,                    // [필수] 취소 사유s
	String storeId                    // [조건부] 하위 상점 사용 시 필수
) {

	public static PortOneCancelRequest of(
		String reason,
		String storeId
	) {
		return new PortOneCancelRequest(reason, storeId);
	}
}