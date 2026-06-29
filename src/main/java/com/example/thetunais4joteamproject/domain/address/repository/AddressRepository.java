package com.example.thetunais4joteamproject.domain.address.repository;

import java.util.List;
import java.util.Optional;

import com.example.thetunais4joteamproject.domain.address.entity.Address;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

	List<Address> findAllByMemberIdOrderByDefaultAddressDescCreatedAtDesc(Long memberId);

	Optional<Address> findByIdAndMemberId(Long addressId, Long memberId);

	Optional<Address> findFirstByMemberIdAndDefaultAddressTrue(Long memberId);

	boolean existsByMemberId(Long memberId);
}