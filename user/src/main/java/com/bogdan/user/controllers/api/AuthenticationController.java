package com.bogdan.user.controllers.api;

import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentications")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/registerUser")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerNewUser(@RequestBody RegisterRequest registerRequest) {
        authenticationService.registerUser(registerRequest);
    }

    @PostMapping("/registerAdmin")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerNewAdmin(@RequestBody RegisterRequest registerRequest) {
        authenticationService.registerAdmin(registerRequest);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return authenticationService.login(loginRequest);
    }

    @GetMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ValidationResponse validateToken() {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();
        return ValidationResponse.builder()
                                 .username(authentication.getName())
                                 .roles(authentication.getAuthorities()
                                                      .stream()
                                                      .map(GrantedAuthority::getAuthority)
                                                      .toList())
                                 .build();
    }
}
