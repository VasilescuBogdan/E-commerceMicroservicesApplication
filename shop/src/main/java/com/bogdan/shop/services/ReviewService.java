package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateReviewDto;
import com.bogdan.shop.controllers.models.GetReviewDetailsDto;
import com.bogdan.shop.controllers.models.UpdateReviewDto;

import java.util.List;

public interface ReviewService {
    void createReview(String name, CreateReviewDto review);

    List<GetReviewDetailsDto> getReviewsSender(String sender);

    void deleteReview(Long id, String sender);

    void updateReview(UpdateReviewDto updateReviewDto, Long id, String sender);
}
