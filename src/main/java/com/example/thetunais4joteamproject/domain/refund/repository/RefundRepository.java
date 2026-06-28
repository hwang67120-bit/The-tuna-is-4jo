package com.example.thetunais4joteamproject.domain.refund.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.thetunais4joteamproject.domain.refund.entity.Refund;
import com.example.thetunais4joteamproject.domain.refund.entity.RefundStatus;

public interface RefundRepository extends JpaRepository<Refund, Long> {

	boolean existsByPaymentIdAndStatusIn(Long paymentId, List<RefundStatus> statuses);

	Optional<Refund> findTopByPaymentIdAndStatusInOrderByRequestedAtDesc(Long paymentId, List<RefundStatus> statuses);
}