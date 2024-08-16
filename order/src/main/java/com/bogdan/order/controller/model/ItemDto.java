package com.bogdan.order.controller.model;

import lombok.Builder;

@Builder
public record ItemDto(String name, Float price) {
}
