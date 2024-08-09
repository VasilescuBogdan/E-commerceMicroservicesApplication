package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record GetReviewProductDto(String message, Integer numberOfStars, String sender) {
}
