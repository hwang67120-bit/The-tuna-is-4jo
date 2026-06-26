package com.example.thetunais4joteamproject.domain.order.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import com.example.thetunais4joteamproject.domain.cart.service.CartService;
import com.example.thetunais4joteamproject.domain.order.dto.OrderPreviewResponse;
import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.entity.ProductOption;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;

import org.junit.jupiter.api.Test;

class OrderFacadeTest {

	@Test
	void 선택한_장바구니_상품으로_주문서_미리보기를_생성한다() {
		// given
		CartService cartService = mock(CartService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		OrderFacade orderFacade = new OrderFacade(cartService, memberRepository);

		Long memberId = 1L;
		List<Long> cartItemIds = List.of(10L, 20L);
		CartItem cartItem = createCartItem(
			1L,
			100L,
			"테스트 상품",
			1000L,
			"XL",
			10000,
			1500,
			2
		);

		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(mock(Member.class)));
		when(cartService.getPreviewItems(memberId, cartItemIds))
			.thenReturn(List.of(cartItem));

		// when
		OrderPreviewResponse response = orderFacade.previewOrder(memberId, cartItemIds);

		// then
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).cartItemId()).isEqualTo(1L);
		assertThat(response.items().get(0).productId()).isEqualTo(100L);
		assertThat(response.items().get(0).productName()).isEqualTo("테스트 상품");
		assertThat(response.items().get(0).productOptionId()).isEqualTo(1000L);
		assertThat(response.items().get(0).optionName()).isEqualTo("XL");
		assertThat(response.items().get(0).unitPrice()).isEqualTo(11500);
		assertThat(response.items().get(0).quantity()).isEqualTo(2);
		assertThat(response.items().get(0).totalPrice()).isEqualTo(23000);

		assertThat(response.orderPrice()).isEqualTo(23000);
		assertThat(response.discountPrice()).isEqualTo(0);
		assertThat(response.deliveryPrice()).isEqualTo(3000);
		assertThat(response.paymentPrice()).isEqualTo(26000);

		verify(memberRepository).findById(memberId);
		verify(cartService).getPreviewItems(memberId, cartItemIds);
		verify(cartItem.getProductOption()).validateEnoughStock(2);
	}

	@Test
	void 장바구니_상품_ID가_없으면_전체_장바구니_상품으로_주문서_미리보기를_생성한다() {
		// given
		CartService cartService = mock(CartService.class);
		MemberRepository memberRepository = mock(MemberRepository.class);
		OrderFacade orderFacade = new OrderFacade(cartService, memberRepository);

		Long memberId = 1L;
		CartItem cartItem = createCartItem(
			1L,
			100L,
			"테스트 상품",
			1000L,
			"XL",
			10000,
			1500,
			2
		);

		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(mock(Member.class)));
		when(cartService.getPreviewItems(memberId, List.of()))
			.thenReturn(List.of(cartItem));

		// when
		OrderPreviewResponse response = orderFacade.previewOrder(memberId, null);

		// then
		assertThat(response.items()).hasSize(1);
		assertThat(response.orderPrice()).isEqualTo(23000);
		assertThat(response.discountPrice()).isEqualTo(0);
		assertThat(response.deliveryPrice()).isEqualTo(3000);
		assertThat(response.paymentPrice()).isEqualTo(26000);

		verify(memberRepository).findById(memberId);
		verify(cartService).getPreviewItems(memberId, List.of());
		verify(cartItem.getProductOption()).validateEnoughStock(2);
	}

	private CartItem createCartItem(
		Long cartItemId,
		Long productId,
		String productName,
		Long productOptionId,
		String optionName,
		Integer productPrice,
		Integer additionalPrice,
		Integer quantity
	) {
		CartItem cartItem = mock(CartItem.class);
		ProductOption productOption = mock(ProductOption.class);
		Product product = mock(Product.class);

		when(cartItem.getId()).thenReturn(cartItemId);
		when(cartItem.getProductOption()).thenReturn(productOption);
		when(cartItem.getQuantity()).thenReturn(quantity);

		when(productOption.getId()).thenReturn(productOptionId);
		when(productOption.getOptionName()).thenReturn(optionName);
		when(productOption.getAdditionalPrice()).thenReturn(additionalPrice);
		when(productOption.getProduct()).thenReturn(product);

		when(product.getId()).thenReturn(productId);
		when(product.getName()).thenReturn(productName);
		when(product.getPrice()).thenReturn(productPrice);

		return cartItem;
	}
}