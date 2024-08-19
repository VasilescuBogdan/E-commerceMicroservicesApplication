package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record GetReviewDetailsDto(String message, Integer numberOfStars, String sender, GetProductDto product) {
}
