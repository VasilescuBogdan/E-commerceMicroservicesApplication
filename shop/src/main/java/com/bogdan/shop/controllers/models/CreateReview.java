package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record CreateReview(long productId, int numberOfStars, String message) {
}
