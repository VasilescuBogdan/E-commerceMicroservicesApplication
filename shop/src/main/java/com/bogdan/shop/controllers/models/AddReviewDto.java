package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record AddReviewDto(long productId, int numberOfStars, String message) {
}
