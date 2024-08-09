package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record GetProductReviewDto(String name, String description, Float price) {
}
