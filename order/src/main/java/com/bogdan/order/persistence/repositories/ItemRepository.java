package com.bogdan.order.persistence.repositories;

import com.bogdan.order.persistence.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}