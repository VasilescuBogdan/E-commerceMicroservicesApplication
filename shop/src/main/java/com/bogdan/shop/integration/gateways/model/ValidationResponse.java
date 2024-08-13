package com.bogdan.shop.integration.gateways.model;

import lombok.Builder;

import java.util.List;

@Builder
public record ValidationResponse(List<String> roles, String username) {
}
