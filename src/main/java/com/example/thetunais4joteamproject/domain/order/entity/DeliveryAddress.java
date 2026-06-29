package com.example.thetunais4joteamproject.domain.order.entity;

import com.example.thetunais4joteamproject.domain.address.entity.Address;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryAddress {

	@Column(nullable = false, length = 50)
	private String receiverName;

	@Column(nullable = false, length = 20)
	private String receiverPhone;

	@Column(nullable = false, length = 10)
	private String zipcode;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private String detailAddress;

	private DeliveryAddress(
		String receiverName,
		String receiverPhone,
		String zipcode,
		String address,
		String detailAddress
	) {
		this.receiverName = receiverName;
		this.receiverPhone = receiverPhone;
		this.zipcode = zipcode;
		this.address = address;
		this.detailAddress = detailAddress;
	}

	public static DeliveryAddress from(Address address) {
		return new DeliveryAddress(
			address.getReceiverName(),
			address.getReceiverPhone(),
			address.getZipcode(),
			address.getAddress(),
			address.getDetailAddress()
		);
	}
}