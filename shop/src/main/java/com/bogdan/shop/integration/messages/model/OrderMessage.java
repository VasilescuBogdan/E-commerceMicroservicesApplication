package com.bogdan.shop.integration.messages.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record OrderMessage(String user, String address, Map<String, Float> orderItem, Long orderNumber) {
}
