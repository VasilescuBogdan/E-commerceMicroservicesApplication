package com.bogdan.shop.persistence.repositories;

import com.bogdan.shop.persistence.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findBySender(@NonNull String sender);
}