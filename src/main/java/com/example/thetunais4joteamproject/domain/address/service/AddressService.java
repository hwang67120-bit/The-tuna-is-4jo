package com.example.thetunais4joteamproject.domain.address.service;

import java.util.ArrayList;
import java.util.List;

import com.example.thetunais4joteamproject.domain.address.dto.CreateAddressRequest;
import com.example.thetunais4joteamproject.domain.address.dto.AddressResponse;
import com.example.thetunais4joteamproject.domain.address.dto.UpdateAddressRequest;
import com.example.thetunais4joteamproject.domain.address.entity.Address;
import com.example.thetunais4joteamproject.domain.address.repository.AddressRepository;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

	private final AddressRepository addressRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public AddressResponse create(Long memberId, CreateAddressRequest request) {
		validateCreateRequest(request);

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));

		boolean firstAddress = !addressRepository.existsByMemberId(memberId);
		boolean defaultAddress = firstAddress || Boolean.TRUE.equals(request.defaultAddress());

		if (defaultAddress) {
			unmarkDefaultAddresses(memberId);
		}

		Address address = Address.of(
			member,
			request.receiverName(),
			request.receiverPhone(),
			request.zipcode(),
			request.address(),
			request.detailAddress(),
			defaultAddress
		);

		return AddressResponse.from(addressRepository.save(address));
	}

	@Transactional(readOnly = true)
	public List<AddressResponse> getAll(Long memberId) {
		List<Address> addresses =
			addressRepository.findAllByMemberIdOrderByDefaultAddressDescCreatedAtDesc(memberId);
		List<AddressResponse> responses = new ArrayList<>();

		for (Address address : addresses) {
			responses.add(AddressResponse.from(address));
		}

		return responses;
	}

	@Transactional
	public AddressResponse update(
		Long memberId,
		Long addressId,
		UpdateAddressRequest request
	) {
		validateUpdateRequest(request);

		Address address = getAddress(memberId, addressId);
		address.update(
			request.receiverName(),
			request.receiverPhone(),
			request.zipcode(),
			request.address(),
			request.detailAddress()
		);

		return AddressResponse.from(address);
	}

	@Transactional
	public AddressResponse changeDefault(Long memberId, Long addressId) {
		Address address = getAddress(memberId, addressId);

		unmarkDefaultAddresses(memberId);
		address.markDefault();

		return AddressResponse.from(address);
	}

	@Transactional
	public void delete(Long memberId, Long addressId) {
		Address address = getAddress(memberId, addressId);
		boolean deletedDefaultAddress = Boolean.TRUE.equals(address.getDefaultAddress());

		addressRepository.delete(address);

		if (deletedDefaultAddress) {
			markFirstAddressAsDefault(memberId, addressId);
		}
	}

	private Address getAddress(Long memberId, Long addressId) {
		return addressRepository.findByIdAndMemberId(addressId, memberId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.ADDRESS_NOT_FOUND));
	}

	private void unmarkDefaultAddresses(Long memberId) {
		List<Address> addresses =
			addressRepository.findAllByMemberIdOrderByDefaultAddressDescCreatedAtDesc(memberId);

		for (Address address : addresses) {
			address.unmarkDefault();
		}
	}

	private void markFirstAddressAsDefault(Long memberId, Long deletedAddressId) {
		List<Address> addresses =
			addressRepository.findAllByMemberIdOrderByDefaultAddressDescCreatedAtDesc(memberId);

		addresses.stream()
			.filter(address -> !address.getId().equals(deletedAddressId))
			.findFirst()
			.ifPresent(Address::markDefault);
	}

	private void validateCreateRequest(CreateAddressRequest request) {
		if (request == null ||
			isBlank(request.receiverName()) ||
			isBlank(request.receiverPhone()) ||
			isBlank(request.zipcode()) ||
			isBlank(request.address()) ||
			isBlank(request.detailAddress())) {
			throw BusinessException.from(ErrorCode.BAD_REQUEST);
		}
	}

	private void validateUpdateRequest(UpdateAddressRequest request) {
		if (request == null ||
			isBlank(request.receiverName()) ||
			isBlank(request.receiverPhone()) ||
			isBlank(request.zipcode()) ||
			isBlank(request.address()) ||
			isBlank(request.detailAddress())) {
			throw BusinessException.from(ErrorCode.BAD_REQUEST);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}