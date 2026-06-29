package com.example.thetunais4joteamproject.domain.address.dto;

import com.example.thetunais4joteamproject.domain.address.entity.Address;

public record AddressResponse(
	Long addressId,
	String receiverName,
	String receiverPhone,
	String zipcode,
	String address,
	String detailAddress,
	Boolean defaultAddress
) {

	public static AddressResponse from(Address address) {
		return new AddressResponse(
			address.getId(),
			address.getReceiverName(),
			address.getReceiverPhone(),
			address.getZipcode(),
			address.getAddress(),
			address.getDetailAddress(),
			address.getDefaultAddress()
		);
	}
}