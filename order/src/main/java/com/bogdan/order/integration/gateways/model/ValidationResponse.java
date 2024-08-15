package com.bogdan.order.integration.gateways.model;

import java.util.List;

public record ValidationResponse(String username, List<String> roles) {
}
