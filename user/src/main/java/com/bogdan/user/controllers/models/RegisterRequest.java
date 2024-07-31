package com.bogdan.user.controllers.models;

import java.io.Serializable;

public record RegisterRequest(String username, String password) implements Serializable {
}