package com.bogdan.shop.persistence.repositories;

import com.bogdan.shop.persistence.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}