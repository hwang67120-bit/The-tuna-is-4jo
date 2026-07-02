package com.example.thetunais4joteamproject.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrderId(Long orderId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.id = :paymentId")
	Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

	// Webhook에서 받아온 portonePaymentId 조건으로 Payment 조회 시 연관된 Order를 fetch join 으로 함께 로딩
	@Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.portonePaymentId = :portonePaymentId")
	Optional<Payment> findByPortonePaymentId(@Param("portonePaymentId") String portonePaymentId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Payment p where p.portonePaymentId = :portonePaymentId")
	Optional<Payment> findByPortonePaymentIdForUpdate(
		@Param("portonePaymentId") String portonePaymentId
	);
}
