package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record GetProductDto(String name, String description, Float price) {
}
