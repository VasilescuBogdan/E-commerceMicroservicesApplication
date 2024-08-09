package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record CreateProductDto(String name, String description, float price) {
}
