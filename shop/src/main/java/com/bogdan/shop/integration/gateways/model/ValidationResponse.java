package com.bogdan.shop.integration.gateways.model;

import lombok.Builder;

@Builder
public record ValidationResponse(String role, String username) {
}
