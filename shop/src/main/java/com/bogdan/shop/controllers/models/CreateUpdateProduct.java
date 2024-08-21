package com.bogdan.shop.controllers.models;

import lombok.Builder;

@Builder
public record CreateUpdateProduct(String name, String description, float price) {
}
