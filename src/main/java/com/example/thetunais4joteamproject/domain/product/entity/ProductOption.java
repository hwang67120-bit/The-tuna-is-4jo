package com.example.thetunais4joteamproject.domain.product.entity;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false, length = 100)
	private String optionName;

	@Column(nullable = false)
	private int optionStock;

	@Column(nullable = false)
	private int additionalPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OptionStatus status;

	// 정적 팩토리 메서드 사용을 위해 기본 생성 구조를 private으로 처리한다.
	private ProductOption(Product product, String optionName, int optionStock, int additionalPrice,
		OptionStatus status) {
		this.product = product;
		this.optionName = optionName;
		this.optionStock = optionStock;
		this.additionalPrice = additionalPrice;
		this.status = status;
	}

	// 정적 팩토리 메서드
	public static ProductOption of(Product product, String optionName, int optionStock, int additionalPrice,
		OptionStatus status) {
		return new ProductOption(product, optionName, optionStock, additionalPrice, status);
	}

	// 비즈니스 로직: 옵션 수정 기능
	public void updateOptionDetails(int optionStock, int additionalPrice, OptionStatus status) {
		this.optionStock = optionStock;
		this.additionalPrice = additionalPrice;
		this.status = status;
	}

	// 비즈니스 로직: 재고 변경 위임 기능
	public void updateStock(int optionStock) {
		this.optionStock = optionStock;
	}

	// 비즈니스 로직: 상태 변경 위임 기능 (재고가 0이 되면 자동으로 SOLDOUT 처리할 때 사용)
	public void changeStatus(OptionStatus status) {
		this.status = status;
	}

	public void decreaseStock(Integer quantity) {
		validateEnoughStock(quantity);

		this.optionStock -= quantity;

		if (this.optionStock == 0) {
			this.status = OptionStatus.SOLDOUT;
		}
	}

	public void increaseStock(Integer quantity) {
		this.optionStock += quantity;

		if (this.status == OptionStatus.SOLDOUT && this.optionStock > 0) {
			this.status = OptionStatus.ON_SALE;
		}
	}

	// 요청 수량이 판매 가능 상태와 재고 범위 안에 있는지 검증
	public void validateEnoughStock(Integer quantity) {
		if (this.optionStock < quantity) {
			throw BusinessException.from(ErrorCode.OUT_OF_STOCK);
		}

		if (this.status != OptionStatus.ON_SALE) {
			throw BusinessException.from(ErrorCode.PRODUCT_OPTION_NOT_ON_SALE);
		}
	}
}
