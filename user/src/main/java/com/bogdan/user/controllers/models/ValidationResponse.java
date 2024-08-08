package com.bogdan.user.controllers.models;

import lombok.Builder;

import java.util.List;

@Builder
public record ValidationResponse(List<String> roles, String username) {
}
