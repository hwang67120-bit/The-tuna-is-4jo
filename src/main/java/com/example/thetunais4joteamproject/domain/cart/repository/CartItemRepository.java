package com.example.thetunais4joteamproject.domain.cart.repository;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	Optional<CartItem> findByCartIdAndProductOptionId(Long cartId, Long productOptionId);

	@Query("""
		SELECT ci
		FROM CartItem ci
		JOIN FETCH ci.productOption po
		JOIN FETCH po.product
		WHERE ci.cart.id = :cartId
		""")
	List<CartItem> findAllByCartIdWithProductOptionAndProduct(@Param("cartId") Long cartId);

	@Query("""
		SELECT ci
		FROM CartItem ci
		JOIN ci.cart c
		JOIN FETCH ci.productOption po
		JOIN FETCH po.product
		WHERE ci.id = :cartItemId
		AND c.member.id = :memberId
		""")
	Optional<CartItem> findByIdAndMemberIdWithProductOptionAndProduct(
		@Param("cartItemId") Long cartItemId,
		@Param("memberId") Long memberId
	);

	@Query("""
		SELECT ci
		FROM CartItem ci
		JOIN ci.cart c
		WHERE ci.id = :cartItemId
		AND c.member.id = :memberId
		""")
	Optional<CartItem> findByIdAndMemberId(
		@Param("cartItemId") Long cartItemId,
		@Param("memberId") Long memberId
	);

	void deleteAllByCartId(Long cartId);

	// 주문서 미리보기 시, 로그인 회원의 전체 장바구니 상품을 조회합니다.
	@Query("""
		SELECT ci
		FROM CartItem ci
		JOIN ci.cart c
		JOIN FETCH ci.productOption po
		JOIN FETCH po.product
		WHERE c.member.id = :memberId
		""")
	List<CartItem> findAllByMemberIdWithProductOptionAndProduct(
		@Param("memberId") Long memberId
	);

	// 주문서 미리보기 시, 로그인 회원이 선택한 장바구니 상품만 조회합니다.
	@Query("""
		SELECT ci
		FROM CartItem ci
		JOIN ci.cart c
		JOIN FETCH ci.productOption po
		JOIN FETCH po.product
		WHERE ci.id IN :cartItemIds
		AND c.member.id = :memberId
		""")
	List<CartItem> findAllByIdInAndMemberIdWithProductOptionAndProduct(
		@Param("cartItemIds") List<Long> cartItemIds,
		@Param("memberId") Long memberId
	);
}