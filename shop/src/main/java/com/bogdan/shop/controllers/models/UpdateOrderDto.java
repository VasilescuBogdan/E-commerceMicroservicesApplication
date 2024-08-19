package com.bogdan.shop.controllers.models;

import java.util.List;

public record UpdateOrderDto(String address, List<Long> productIds) {
}
