package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record UpdateReviewDto(String message, int numberOfStars) {
}
