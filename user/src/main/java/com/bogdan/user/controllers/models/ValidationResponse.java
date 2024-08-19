package com.bogdan.user.controllers.models;

import lombok.Builder;

@Builder
public record ValidationResponse(String role, String username) {
}
