package com.bogdan.order.integration.messages.model;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderDetails(String user, String address, List<OrderItem> orderItem, Long orderNumber) {
}
