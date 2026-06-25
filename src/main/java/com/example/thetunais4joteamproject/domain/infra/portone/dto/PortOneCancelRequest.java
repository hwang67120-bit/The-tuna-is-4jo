package com.example.thetunais4joteamproject.domain.infra.portone.dto;

public record PortOneCancelRequest(
	String reason,                    // [필수] 취소 사유
	Integer amount,                   // [선택] 취소 금액. null이면 전액 취소
	String storeId                    // [조건부] 하위 상점 사용 시 필수
) {

	public static PortOneCancelRequest of(
		String reason,
		Integer amount,
		String storeId
	) {
		return new PortOneCancelRequest(reason, amount, storeId);
	}
}