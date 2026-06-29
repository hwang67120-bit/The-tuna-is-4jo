package com.example.thetunais4joteamproject.domain.address.dto;

public record CreateAddressRequest(
	String receiverName,
	String receiverPhone,
	String zipcode,
	String address,
	String detailAddress,
	Boolean defaultAddress
) {
}