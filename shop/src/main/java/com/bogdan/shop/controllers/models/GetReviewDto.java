package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record GetReviewDto(String message, Integer numberOfStars, String sender, GetProductReviewDto product) {
}
