package com.example.thetunais4joteamproject.domain.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.thetunais4joteamproject.domain.order.entity.Order;
public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

	List<Order> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
