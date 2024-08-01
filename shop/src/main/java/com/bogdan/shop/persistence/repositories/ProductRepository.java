package com.bogdan.shop.persistence.repositories;

import com.bogdan.shop.persistence.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}