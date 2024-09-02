package com.bogdan.shop.persistence.repositories;

import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByReviewsContains(List<Review> reviews);
}