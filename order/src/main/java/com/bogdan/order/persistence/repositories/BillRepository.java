package com.bogdan.order.persistence.repositories;

import com.bogdan.order.persistence.entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {
}