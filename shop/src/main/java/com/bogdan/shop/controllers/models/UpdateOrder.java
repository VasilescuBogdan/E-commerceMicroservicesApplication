package com.bogdan.shop.controllers.models;

import java.util.List;

public record UpdateOrder(String address, List<Long> productIds) {
}
