package com.bogdan.user.controllers;

import com.bogdan.user.config.SecurityConfig;
import com.bogdan.user.controllers.api.AuthenticationController;
import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.service.AuthenticationService;
import com.bogdan.user.service.impl.JwtServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService service;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                 .alwaysDo(MockMvcResultHandlers.log())
                                 .apply(SecurityMockMvcConfigurers.springSecurity())
                                 .build();
    }

    @Test
    void registerNewUser_registerRequestProvided_returnStatusCreated() throws Exception {
        //Arrange
        RegisterRequest request = RegisterRequest.builder()
                                                 .username("alen")
                                                 .password("pass")
                                                 .build();

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/authentications/register-user")
                                                                       .contentType(MediaType.APPLICATION_JSON)
                                                                       .content(objectMapper.writeValueAsString(
                                                                               request)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isCreated());
    }

    @Test
    void registerNewAdmin_registerRequestProvided_returnStatusCreated() throws Exception {
        //Arrange
        RegisterRequest request = RegisterRequest.builder()
                                                 .username("alen")
                                                 .password("pass")
                                                 .build();

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/authentications/register-admin")
                                                                       .contentType(MediaType.APPLICATION_JSON)
                                                                       .content(objectMapper.writeValueAsString(
                                                                               request)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isCreated());
    }

    @Test
    void login_loginRequestProvided_returnsTokenAndOkStatus() throws Exception {
        //Arrange
        LoginRequest request = LoginRequest.builder()
                                           .username("alen")
                                           .password("pass")
                                           .build();
        LoginResponse loginResponse = new LoginResponse("token");
        Mockito.when(service.login(request))
               .thenReturn(loginResponse);

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/authentications/login")
                                                                       .contentType(MediaType.APPLICATION_JSON)
                                                                       .content(objectMapper.writeValueAsString(
                                                                               request)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("token")
                                                .value("token"));
    }

    @Test
    void login_serviceReturnsAuthenticationException_returnStatusUnauthorised() throws Exception {
        //Arrange
        LoginRequest request = LoginRequest.builder()
                                           .username("user")
                                           .password("password")
                                           .build();

        Mockito.doThrow(BadCredentialsException.class)
               .when(service)
               .login(request);

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/authentications/login")
                                                                       .contentType(MediaType.APPLICATION_JSON)
                                                                       .content(objectMapper.writeValueAsString(
                                                                               request)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isUnauthorized());
    }

    @Test
    @WithMockUser
    void validate_validTokenProvided_returnsUserDetailsAndOkStatus() throws Exception {
        //Arrange
        ValidationResponse validationResponse = ValidationResponse.builder()
                                                                  .username("bob")
                                                                  .role("USER")
                                                                  .build();
        Authentication auth = SecurityContextHolder.getContext()
                                                   .getAuthentication();
        Mockito.when(service.getValidationResponse(auth))
               .thenReturn(validationResponse);

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/authentications/validate"));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("username")
                                                .value("bob"))
                .andExpect(MockMvcResultMatchers.jsonPath("role")
                                                .value("USER"));
    }
}

