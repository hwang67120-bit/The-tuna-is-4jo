package com.example.thetunais4joteamproject.domain.cart.repository;

import com.example.thetunais4joteamproject.domain.cart.entity.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	Optional<CartItem> findByCartIdAndProductOptionId(Long cartId, Long productOptionId);
}