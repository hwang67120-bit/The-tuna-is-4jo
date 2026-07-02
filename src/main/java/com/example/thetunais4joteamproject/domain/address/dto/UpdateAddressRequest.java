package com.example.thetunais4joteamproject.domain.address.dto;

public record UpdateAddressRequest(
	String receiverName,
	String receiverPhone,
	String zipcode,
	String address,
	String detailAddress
) {
}