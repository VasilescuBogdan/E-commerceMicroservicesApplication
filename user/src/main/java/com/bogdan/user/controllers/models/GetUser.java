package com.bogdan.user.controllers.models;

import com.bogdan.user.persistence.entities.enums.Role;
import lombok.Builder;

@Builder
public record GetUser(String username, String password, Role role) {
}