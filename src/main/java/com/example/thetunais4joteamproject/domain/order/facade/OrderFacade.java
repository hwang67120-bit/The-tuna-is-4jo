package com.example.thetunais4joteamproject.domain.order.facade;

import java.util.ArrayList;
import java.util.List;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.cart.service.CartService;
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewItemResponse;
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewResponse;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacade {

	private final CartService cartService;
	private final MemberRepository memberRepository;

	@Transactional(readOnly = true)
	public OrderPreviewResponse previewOrder(Long memberId, List<Long> cartItemIds) {
		validateMemberExists(memberId);

		List<Long> previewCartItemIds = getCartItemIds(cartItemIds);
		List<CartItem> cartItems = cartService.getPreviewItems(memberId, previewCartItemIds);
		List<OrderPreviewItemResponse> items = getOrderPreviewItems(cartItems);

		// 쿠폰 도메인이 아직 구현되지 않았으므로 할인 금액은 0원, 배송비는 기본 정책인 3000원을 적용합니다.
		return OrderPreviewResponse.of(items, 0, 3000);
	}

	private void validateMemberExists(Long memberId) {
		memberRepository.findById(memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));
	}

	private List<OrderPreviewItemResponse> getOrderPreviewItems(List<CartItem> cartItems) {
		List<OrderPreviewItemResponse> items = new ArrayList<>();

		for (CartItem cartItem : cartItems) {
			cartItem.getProductOption().validateEnoughStock(cartItem.getQuantity());
			items.add(OrderPreviewItemResponse.from(cartItem));
		}

		return items;
	}

	private List<Long> getCartItemIds(List<Long> cartItemIds) {
		return cartItemIds != null ? cartItemIds : List.of();
	}
}