package com.example.thetunais4joteamproject.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}