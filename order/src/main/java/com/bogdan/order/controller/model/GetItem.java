package com.bogdan.order.controller.model;

import lombok.Builder;

@Builder
public record GetItem(String name, Float price) {
}
