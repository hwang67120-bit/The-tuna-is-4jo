package com.example.thetunais4joteamproject.domain.payment.service;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;

public record PaymentConfirmContext(
	Long paymentId,
	String portonePaymentId,
	Integer pgAmount,
	boolean alreadyPaid
) {

	public static PaymentConfirmContext from(Payment payment, boolean alreadyPaid) {
		return new PaymentConfirmContext(
			payment.getId(),
			payment.getPortonePaymentId(),
			payment.getPgAmount(),
			alreadyPaid
		);
	}
}
