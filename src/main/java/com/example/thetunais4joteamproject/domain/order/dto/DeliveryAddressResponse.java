package com.example.thetunais4joteamproject.domain.order.dto;

import com.example.thetunais4joteamproject.domain.order.entity.DeliveryAddress;

public record DeliveryAddressResponse(
	String receiverName,
	String receiverPhone,
	String zipcode,
	String address,
	String detailAddress
) {

	public static DeliveryAddressResponse from(DeliveryAddress deliveryAddress) {
		if (deliveryAddress == null) {
			return null;
		}

		return new DeliveryAddressResponse(
			deliveryAddress.getReceiverName(),
			deliveryAddress.getReceiverPhone(),
			deliveryAddress.getZipcode(),
			deliveryAddress.getAddress(),
			deliveryAddress.getDetailAddress()
		);
	}
}