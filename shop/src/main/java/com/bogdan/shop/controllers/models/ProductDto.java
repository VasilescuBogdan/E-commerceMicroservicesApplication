package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record ProductDto(String name, String description, float price) {
}
