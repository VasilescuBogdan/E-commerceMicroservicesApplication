package com.bogdan.user.controllers.models;

import com.bogdan.user.utils.enums.Role;
import lombok.Builder;

@Builder
public record UserDto(String username, String password, Role role) {
}