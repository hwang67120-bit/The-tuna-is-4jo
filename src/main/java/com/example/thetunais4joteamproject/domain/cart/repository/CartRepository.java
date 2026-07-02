package com.example.thetunais4joteamproject.domain.cart.repository;

import com.example.thetunais4joteamproject.domain.cart.entity.Cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

	Optional<Cart> findByMemberId(Long memberId);
}