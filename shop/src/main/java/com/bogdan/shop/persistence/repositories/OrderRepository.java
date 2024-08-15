package com.bogdan.shop.persistence.repositories;

import com.bogdan.shop.persistence.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(String user);
}