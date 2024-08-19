package com.bogdan.shop.integration.messages.model;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderMessage(String user, String address, List<OrderItem> orderItem, Long orderNumber) {
}
