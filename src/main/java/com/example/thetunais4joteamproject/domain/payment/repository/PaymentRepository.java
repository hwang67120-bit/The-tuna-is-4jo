package com.example.thetunais4joteamproject.domain.payment.repository;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}