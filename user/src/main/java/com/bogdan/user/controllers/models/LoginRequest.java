package com.bogdan.user.controllers.models;

import lombok.Builder;

@Builder
public record LoginRequest(String username, String password) {
}
