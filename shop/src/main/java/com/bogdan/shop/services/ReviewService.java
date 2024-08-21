package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateReview;
import com.bogdan.shop.controllers.models.GetReviewDetails;
import com.bogdan.shop.controllers.models.UpdateReview;

import java.util.List;

public interface ReviewService {
    void createReview(String name, CreateReview review);

    List<GetReviewDetails> getReviewsSender(String sender);

    void deleteReview(Long id, String sender);

    void updateReview(UpdateReview updateReview, Long id, String sender);
}
