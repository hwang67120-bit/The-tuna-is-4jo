package com.example.thetunais4joteamproject.domain.product.dto;

import com.example.thetunais4joteamproject.domain.product.entity.OptionStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateOptionRequest {
	
	@NotNull(message = "옵션 ID는 필수 입력 값입니다.")
    private Long optionId;

    @Min(value = 0, message = "옵션 재고는 0개 이상이어야 합니다.")
    private int optionStock;

    @Min(value = 0, message = "추가 금액은 0원 이상이어야 합니다.")
    private int additionalPrice;

    @NotNull(message = "옵션 상태값은 필수입니다.")
    private OptionStatus status;

	private UpdateOptionRequest(Long optionId, int optionStock, int additionalPrice, OptionStatus status) {
        this.optionId = optionId;
        this.optionStock = optionStock;
        this.additionalPrice = additionalPrice;
        this.status = status;
    }

    // 정적 팩토리 메서드 공통 규칙을 반영
    public static UpdateOptionRequest of(Long optionId, int optionStock, int additionalPrice, OptionStatus status) {
        return new UpdateOptionRequest(optionId, optionStock, additionalPrice, status);
    }
}
