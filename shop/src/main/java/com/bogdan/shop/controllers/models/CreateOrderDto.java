package com.bogdan.shop.controllers.models;

import java.util.List;

public record CreateOrderDto(String address, List<Long> productIds) {
}
