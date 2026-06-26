package com.example.thetunais4joteamproject.domain.payment.repository;

import java.util.Optional;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrderId(Long orderId);
}