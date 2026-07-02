package com.example.thetunais4joteamproject.domain.order.repository;

import java.util.List;

import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;
import com.example.thetunais4joteamproject.domain.order.entity.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	@Query("""
			SELECT oi
			FROM OrderItem oi
			JOIN FETCH oi.productOption
			WHERE oi.order.id = :orderId
		""")
	List<OrderItem> findAllByOrderIdWithProductOption(@Param("orderId") Long orderId);

	@Query("""
			SELECT COUNT(oi) > 0
			FROM OrderItem oi
			JOIN oi.order o
			WHERE oi.cartItemId IN :cartItemIds
			AND o.member.id = :memberId
			AND o.status = :status
		""")
	boolean existsByCartItemIdsAndMemberIdAndOrderStatus(
		@Param("cartItemIds") List<Long> cartItemIds,
		@Param("memberId") Long memberId,
		@Param("status") OrderStatus status
	);
}