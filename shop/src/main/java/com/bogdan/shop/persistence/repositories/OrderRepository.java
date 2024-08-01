package com.bogdan.shop.persistence.repositories;

import com.bogdan.shop.persistence.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}