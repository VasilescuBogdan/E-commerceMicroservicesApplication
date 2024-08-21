package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record GetProduct(String name, String description, Float price) {
}
