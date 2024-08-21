package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record UpdateReview(String message, int numberOfStars) {
}
