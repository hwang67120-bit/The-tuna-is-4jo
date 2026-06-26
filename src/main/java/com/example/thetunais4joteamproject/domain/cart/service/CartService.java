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
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

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

	public CartItem updateCartItemQuantity(Long memberId, Long cartItemId, Integer quantity) {
		CartItem cartItem = cartItemRepository.findByIdAndMemberIdWithProductOptionAndProduct(
				cartItemId,
				memberId
			)
			.orElseThrow(() -> BusinessException.from(ErrorCode.CART_ITEM_NOT_FOUND));

		ProductOption productOption = cartItem.getProductOption();

		productOption.validateEnoughStock(quantity);
		cartItem.updateQuantity(quantity);

		return cartItem;
	}

	public void clearCart(Long memberId) {
		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.CART_NOT_FOUND));

		cartItemRepository.deleteAllByCartId(cart.getId());
	}

	public void deleteCartItem(Long memberId, Long cartItemId) {
		CartItem cartItem = cartItemRepository.findByIdAndMemberId(
				cartItemId,
				memberId
			)
			.orElseThrow(() -> BusinessException.from(ErrorCode.CART_ITEM_NOT_FOUND));

		cartItemRepository.delete(cartItem);
	}

	public GetCartResponse getCart(Long memberId) {
		return cartRepository.findByMemberId(memberId)
			.map(this::getCartResponse)
			.orElseGet(GetCartResponse::empty);
	}

	// 주문서 미리보기에 사용할 장바구니 상품 조회
	public List<CartItem> getPreviewItems(Long memberId, List<Long> cartItemIds) {
		List<CartItem> cartItems = hasSelectedCartItemIds(cartItemIds)
			? getSelectedPreviewItems(memberId, cartItemIds)
			: getAllPreviewItems(memberId);

		validateCartNotEmpty(cartItems);

		return cartItems;
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

	private List<CartItem> getSelectedPreviewItems(Long memberId, List<Long> cartItemIds) {
		List<CartItem> cartItems =
			cartItemRepository.findAllByIdInAndMemberIdWithProductOptionAndProduct(
				cartItemIds,
				memberId
			);

		validateSelectedCartItems(cartItems, cartItemIds);

		return cartItems;
	}

	private List<CartItem> getAllPreviewItems(Long memberId) {
		return cartItemRepository.findAllByMemberIdWithProductOptionAndProduct(memberId);
	}

	private boolean hasSelectedCartItemIds(List<Long> cartItemIds) {
		return cartItemIds != null && !cartItemIds.isEmpty();
	}

	private void validateSelectedCartItems(List<CartItem> cartItems, List<Long> cartItemIds) {
		if (cartItems.size() != cartItemIds.size()) {
			throw BusinessException.from(ErrorCode.CART_ITEM_NOT_FOUND);
		}
	}

	private void validateCartNotEmpty(List<CartItem> cartItems) {
		if (cartItems.isEmpty()) {
			throw BusinessException.from(ErrorCode.CART_EMPTY);
		}
	}
}