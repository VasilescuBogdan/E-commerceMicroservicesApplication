package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record CreateUpdateProductDto(String name, String description, float price) {
}
