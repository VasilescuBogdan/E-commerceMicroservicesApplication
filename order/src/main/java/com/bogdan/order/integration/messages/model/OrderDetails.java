package com.bogdan.order.integration.messages.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record OrderDetails(String user, String address, Map<String, Float> orderItem, Long orderNumber) {
}
