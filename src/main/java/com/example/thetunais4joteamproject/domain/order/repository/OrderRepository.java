package com.example.thetunais4joteamproject.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.thetunais4joteamproject.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
