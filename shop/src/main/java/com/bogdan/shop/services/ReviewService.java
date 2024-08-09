package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.AddReviewDto;
import com.bogdan.shop.controllers.models.GetReviewDto;
import com.bogdan.shop.controllers.models.UpdateReviewDto;

import java.util.List;

public interface ReviewService {
    void addReview(String name, AddReviewDto review);

    List<GetReviewDto> getReviewsSender(String sender);

    void deleteReview(Long id, String sender);

    void updateReview(UpdateReviewDto updateReviewDto, Long id, String sender);
}
