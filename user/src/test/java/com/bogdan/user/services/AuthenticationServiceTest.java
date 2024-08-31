package com.bogdan.user.services;

import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.repositories.UserRepository;
import com.bogdan.user.service.JwtService;
import com.bogdan.user.service.impl.AuthenticationServiceImpl;
import com.bogdan.user.persistence.entities.enums.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthenticationServiceImpl service;

    @Test
    void registerAdmin_repositorySaveIsCalled() {
        //Arrange
        RegisterRequest request = new RegisterRequest("user", "password");
        doReturn("encoded password").when(encoder)
                                    .encode(request.password());
        User user = new User(null, request.username(), "encoded password", Role.ADMIN);

        //Act
        service.registerAdmin(request);

        //Assert
        verify(repository, times(1)).save(user);
    }

    @Test
    void registerUser_repositorySaveIsCalled() {
        //Arrange
        RegisterRequest request = new RegisterRequest("user", "password");
        doReturn("encodedPassword").when(encoder)
                                   .encode(request.password());
        User user = new User(null, request.username(), "encodedPassword", Role.USER);

        //Act
        service.registerUser(request);

        //Assert
        verify(repository, times(1)).save(user);
    }

    @Test
    void login_userIsAuthenticated_returnLoginResponse() {
        //Arrange
        LoginRequest request = LoginRequest.builder()
                                           .username("username")
                                           .password("password")
                                           .build();
        String token = "token";
        User user = new User(1L, request.username(), request.password(), Role.USER);
        Authentication authentication = mock(Authentication.class);
        doReturn(authentication).when(authenticationManager)
                                .authenticate(new UsernamePasswordAuthenticationToken(request.username(),
                                        request.password()));
        doReturn(true).when(authentication)
                      .isAuthenticated();
        doReturn(Optional.of(user)).when(repository)
                                   .findByUsername(request.username());
        doReturn(token).when(jwtService)
                       .generateToken(user);

        //Act
        LoginResponse response = service.login(request);

        //Assert
        Assertions.assertThat(response.token())
                  .isNotNull()
                  .isEqualTo(token);
    }

    @Test
    void login_userIsNotAuthenticated_ThrowBadCredentialsException() {
        //Arrange
        LoginRequest request = LoginRequest.builder()
                                           .username("username")
                                           .password("password")
                                           .build();
        Authentication authentication = mock(Authentication.class);
        doReturn(authentication).when(authenticationManager)
                                .authenticate(new UsernamePasswordAuthenticationToken(request.username(),
                                        request.password()));
        doReturn(false).when(authentication)
                       .isAuthenticated();

        //Assert
        Assertions.assertThatExceptionOfType(BadCredentialsException.class)
                  .isThrownBy(() -> {
                      //Act
                      service.login(request);
                  });
    }

    @Test
    void getValidationResponse() {
        //Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("mario", "",
                List.of(new SimpleGrantedAuthority("ADMIN")));
        SecurityContext context = mock(SecurityContext.class);
        doReturn(authentication).when(context)
                                .getAuthentication();
        SecurityContextHolder.setContext(context);

        //Act
        ValidationResponse response = service.getValidationResponse();

        //Assert
        Assertions.assertThat(response.username())
                  .isEqualTo("mario");
        Assertions.assertThat(response.role())
                  .isEqualTo("ADMIN");
    }
}
