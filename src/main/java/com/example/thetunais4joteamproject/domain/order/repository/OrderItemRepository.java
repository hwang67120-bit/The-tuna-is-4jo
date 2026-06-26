package com.example.thetunais4joteamproject.domain.order.repository;

import java.util.List;

import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;

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
}