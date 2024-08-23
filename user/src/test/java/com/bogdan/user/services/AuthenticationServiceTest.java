package com.bogdan.user.services;

import com.bogdan.user.config.SecurityConfig;
import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.repositories.UserRepository;
import com.bogdan.user.service.JwtService;
import com.bogdan.user.service.impl.AuthenticationServiceImpl;
import com.bogdan.user.utils.enums.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl service;

    @Test
    void login_userIsAuthenticated_returnLoginResponse() {
        //Arrange
        LoginRequest request = LoginRequest.builder()
                                           .username("username")
                                           .password("password")
                                           .build();
        String token = "token";
        User user = new User(1L, request.username(), request.password(), Role.USER);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationManager.authenticate(
                       new UsernamePasswordAuthenticationToken(request.username(), request.password())))
               .thenReturn(authentication);
        Mockito.when(authentication.isAuthenticated())
               .thenReturn(true);
        Mockito.when(userRepository.findByUsername(request.username()))
               .thenReturn(Optional.of(user));
        Mockito.when(jwtService.generateToken(user))
               .thenReturn(token);

        //Act
        LoginResponse response = service.login(request);

        //Assert
        Assertions.assertEquals(token, response.token());
    }

    @Test
    void login_userIsNotAuthenticated_ThrowBadCredentialsException() {
        //Arrange
        LoginRequest request = LoginRequest.builder()
                                           .username("username")
                                           .password("password")
                                           .build();
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationManager.authenticate(
                       new UsernamePasswordAuthenticationToken(request.username(), request.password())))
               .thenReturn(authentication);
        Mockito.when(authentication.isAuthenticated())
               .thenReturn(false);

        //Assert
        Assertions.assertThrows(BadCredentialsException.class, () -> {
            //Act
            service.login(request);
        });
    }

    @Test
    void getValidationResponse() {
        //Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("mario", "",
                List.of(new SimpleGrantedAuthority("ADMIN")));

        //Act
        ValidationResponse response = service.getValidationResponse(authentication);

        //Assert
        Assertions.assertEquals("mario", response.username());
        Assertions.assertEquals("ADMIN", response.role());
    }
}
