package com.bogdan.shop.controllers.models;

import lombok.Builder;

import java.util.List;

@Builder
public record GetProductDto(String name, String description, Float price, List<GetReviewProductDto> reviews) {
}
