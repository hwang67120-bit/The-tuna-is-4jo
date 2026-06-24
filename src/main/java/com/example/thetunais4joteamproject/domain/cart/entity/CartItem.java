package com.example.thetunais4joteamproject.domain.cart.entity;

import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.global.common.BaseEntity;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "cart_item",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_cart_item_cart_product_option",
			columnNames = {"cart_id", "product_option_id"}
		)
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_option_id", nullable = false)
	private ProductOption productOption;

	@Column(nullable = false)
	private Integer quantity;

	private CartItem(Cart cart, ProductOption productOption, Integer quantity) {
		this.cart = cart;
		this.productOption = productOption;
		this.quantity = quantity;
	}

	public static CartItem of(Cart cart, ProductOption productOption, Integer quantity) {
		return new CartItem(cart, productOption, quantity);
	}

	public void increaseQuantity(Integer quantity) {
		if (quantity == null || quantity < 1) {
			throw BusinessException.from(ErrorCode.INVALID_CART_ITEM_QUANTITY);
		}

		this.quantity += quantity;
	}
}