package com.bogdan.shop.controllers.models;

import lombok.Builder;

import java.util.List;

@Builder
public record GetProductDetailsDto(String name, String description, Float price, List<GetReviewDto> reviews) {
}
