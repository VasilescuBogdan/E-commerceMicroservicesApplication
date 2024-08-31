package com.bogdan.user.service;

import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;

public interface AuthenticationService {

    void registerUser(RegisterRequest request);

    void registerAdmin(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    ValidationResponse getValidationResponse();
}
