package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record CreateReviewDto(long productId, int numberOfStars, String message) {
}
