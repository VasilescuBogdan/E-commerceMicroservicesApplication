package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record GetReviewDetails(String message, Integer numberOfStars, String sender, GetProduct product) {
}
