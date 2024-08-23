package com.bogdan.user.service.impl;

import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.service.JwtService;
import com.bogdan.user.service.AuthenticationService;
import com.bogdan.user.persistence.entities.enums.Role;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @Override
    public void registerUser(RegisterRequest request) {
        register(request, Role.USER);
    }

    @Override
    public void registerAdmin(RegisterRequest request) {
        register(request, Role.ADMIN);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        if (!authentication.isAuthenticated()) {
            throw new BadCredentialsException("Invalid credentials");
        }
        User user = userRepository.findByUsername(request.username())
                                  .get();
        return new LoginResponse(jwtService.generateToken(user));
    }

    @Override
    public ValidationResponse getValidationResponse(Authentication authentication) {
        return ValidationResponse.builder()
                                 .username(authentication.getName())
                                 .role(authentication.getAuthorities()
                                                     .stream()
                                                     .map(GrantedAuthority::getAuthority)
                                                     .findAny()
                                                     .orElseThrow())
                                 .build();
    }

    private void register(RegisterRequest request, Role role) {
        User newUser = User.builder()
                           .username(request.username())
                           .password(encoder.encode(request.password()))
                           .role(role)
                           .build();
        userRepository.save(newUser);
    }
}
