package com.example.thetunais4joteamproject.domain.product.repository;

import com.example.thetunais4joteamproject.domain.product.entity.Product;
import com.example.thetunais4joteamproject.domain.product.repository.custom.ProductRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
