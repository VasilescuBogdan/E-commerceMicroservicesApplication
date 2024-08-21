package com.bogdan.shop.controllers.models;

import java.util.List;

public record CreateOrder(String address, List<Long> productIds) {
}
