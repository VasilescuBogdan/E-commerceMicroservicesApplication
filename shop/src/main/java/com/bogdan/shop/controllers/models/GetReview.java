package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record GetReview(String message, Integer numberOfStars, String sender) {
}
