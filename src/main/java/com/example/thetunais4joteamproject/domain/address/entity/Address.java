package com.example.thetunais4joteamproject.domain.address.entity;

import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member_address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

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

	@Column(nullable = false)
	private Boolean defaultAddress;

	private Address(
		Member member,
		String receiverName,
		String receiverPhone,
		String zipcode,
		String address,
		String detailAddress,
		Boolean defaultAddress
	) {
		this.member = member;
		this.receiverName = receiverName;
		this.receiverPhone = receiverPhone;
		this.zipcode = zipcode;
		this.address = address;
		this.detailAddress = detailAddress;
		this.defaultAddress = defaultAddress;
	}

	public static Address of(
		Member member,
		String receiverName,
		String receiverPhone,
		String zipcode,
		String address,
		String detailAddress,
		Boolean defaultAddress
	) {
		return new Address(
			member,
			receiverName,
			receiverPhone,
			zipcode,
			address,
			detailAddress,
			defaultAddress
		);
	}

	public void update(
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

	public void markDefault() {
		this.defaultAddress = true;
	}

	public void unmarkDefault() {
		this.defaultAddress = false;
	}
}