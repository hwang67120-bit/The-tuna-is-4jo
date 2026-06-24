package com.example.thetunais4joteamproject.domain.cart.service;

import java.util.ArrayList;
import java.util.List;

import com.example.thetunais4joteamproject.domain.cart.dto.GetCartItemResponse;
import com.example.thetunais4joteamproject.domain.cart.dto.GetCartResponse;
import com.example.thetunais4joteamproject.domain.cart.entity.Cart;
import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.cart.repository.CartItemRepository;
import com.example.thetunais4joteamproject.domain.cart.repository.CartRepository;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.user.entity.Member;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;

	public Cart getOrCreateCart(Member member) {
		return cartRepository.findByMemberId(member.getId())
			.orElseGet(() -> cartRepository.save(Cart.of(member)));
	}

	public CartItem createOrIncreaseCartItem(
		Cart cart,
		ProductOption productOption,
		Integer quantity
	) {
		CartItem cartItem = cartItemRepository
			.findByCartIdAndProductOptionId(cart.getId(), productOption.getId())
			.orElseGet(() -> cartItemRepository.save(
				CartItem.of(cart, productOption, 0)
			));

		int totalQuantity = cartItem.getQuantity() + quantity;

		productOption.validateEnoughStock(totalQuantity);
		cartItem.increaseQuantity(quantity);

		return cartItem;
	}

	public GetCartResponse getCart(Long memberId) {
		return cartRepository.findByMemberId(memberId)
			.map(this::getCartResponse)
			.orElseGet(GetCartResponse::empty);
	}

	private GetCartResponse getCartResponse(Cart cart) {
		List<CartItem> cartItems =
			cartItemRepository.findAllByCartIdWithProductOptionAndProduct(cart.getId());

		List<GetCartItemResponse> items = new ArrayList<>();

		for (CartItem cartItem : cartItems) {
			items.add(GetCartItemResponse.from(cartItem));
		}

		return GetCartResponse.of(items);
	}
}