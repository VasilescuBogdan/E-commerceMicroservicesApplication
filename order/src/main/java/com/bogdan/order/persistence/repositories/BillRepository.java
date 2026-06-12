package com.bogdan.order.persistence.repositories;

import com.bogdan.order.persistence.entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByUser(String user);
}