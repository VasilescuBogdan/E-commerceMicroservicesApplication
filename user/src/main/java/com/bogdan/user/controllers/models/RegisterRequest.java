package com.bogdan.user.controllers.models;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record RegisterRequest(String username, String password) implements Serializable {
}