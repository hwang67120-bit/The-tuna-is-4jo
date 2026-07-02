package com.example.thetunais4joteamproject.domain.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
import com.example.thetunais4joteamproject.domain.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

	List<Order> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

	@Query("""
			SELECT o
			FROM Order o
			WHERE o.status = :status
			AND o.createdAt < :expiredAt
		""")
	List<Order> findExpiredPendingOrders(
		@Param("status") OrderStatus status,
		@Param("expiredAt") LocalDateTime expiredAt
	);
}
