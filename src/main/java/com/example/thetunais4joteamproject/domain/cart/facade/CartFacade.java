package com.example.thetunais4joteamproject.domain.cart.facade;

import com.example.thetunais4joteamproject.domain.cart.dto.CreateCartItemRequest;
import com.example.thetunais4joteamproject.domain.cart.dto.CreateCartItemResponse;
import com.example.thetunais4joteamproject.domain.cart.dto.GetCartResponse;
import com.example.thetunais4joteamproject.domain.cart.entity.Cart;
import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.cart.service.CartService;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.product.repository.ProductOptionRepository;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CartFacade {

	private final CartService cartService;
	private final MemberRepository memberRepository;
	private final ProductOptionRepository productOptionRepository;

	@Transactional
	public CreateCartItemResponse createCartItem(Long memberId, CreateCartItemRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

		ProductOption productOption = productOptionRepository.findById(request.productOptionId())
			.orElseThrow(() -> BusinessException.from(ErrorCode.OPTION_NOT_FOUND));

		Cart cart = cartService.getOrCreateCart(member);
		CartItem cartItem = cartService.createOrIncreaseCartItem(
			cart,
			productOption,
			request.quantity()
		);

		return CreateCartItemResponse.from(cartItem);
	}

	@Transactional(readOnly = true)
	public GetCartResponse getCart(Long memberId) {
		memberRepository.findById(memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

		return cartService.getCart(memberId);
	}
}