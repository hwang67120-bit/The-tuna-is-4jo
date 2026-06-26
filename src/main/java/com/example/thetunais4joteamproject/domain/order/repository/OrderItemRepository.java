package com.example.thetunais4joteamproject.domain.order.repository;

import com.example.thetunais4joteamproject.domain.order.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}