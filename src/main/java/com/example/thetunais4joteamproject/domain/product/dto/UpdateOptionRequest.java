package com.example.thetunais4joteamproject.domain.product.dto;

import com.example.thetunais4joteamproject.domain.product.entity.OptionStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateOptionRequest {

    private Long optionId;
    private int optionStock;
    private int additionalPrice;
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
