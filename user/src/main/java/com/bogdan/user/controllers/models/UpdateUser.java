package com.bogdan.user.controllers.models;

import lombok.Builder;

@Builder
public record UpdateUser(String username, String password) {
}