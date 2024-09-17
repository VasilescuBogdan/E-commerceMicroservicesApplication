package com.bogdan.user.controllers;

import com.bogdan.user.controllers.api.AuthenticationController;
import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.service.AuthenticationService;
import com.bogdan.user.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService service;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
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
        response.andExpect(status().isCreated());
        verify(service, times(1)).registerUser(request);
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
        response.andExpect(status().isCreated());
        verify(service, times(1)).registerAdmin(request);
    }

    @Test
    void login_loginRequestProvided_returnsTokenAndOkStatus() throws Exception {
        //Arrange
        LoginRequest request = LoginRequest.builder()
                                           .username("alen")
                                           .password("pass")
                                           .build();
        LoginResponse loginResponse = new LoginResponse("token");
        doReturn(loginResponse).when(service)
                               .login(request);

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/authentications/login")
                                                                       .contentType(MediaType.APPLICATION_JSON)
                                                                       .content(objectMapper.writeValueAsString(
                                                                               request)));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(loginResponse)));
    }

    @Test
    void login_serviceReturnsAuthenticationException_returnStatusUnauthorised() throws Exception {
        //Arrange
        LoginRequest request = LoginRequest.builder()
                                           .username("user")
                                           .password("password")
                                           .build();

        doThrow(BadCredentialsException.class).when(service)
                                              .login(request);

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/authentications/login")
                                                                       .contentType(MediaType.APPLICATION_JSON)
                                                                       .content(objectMapper.writeValueAsString(
                                                                               request)));

        //Assert
        response.andExpect(status().isUnauthorized());
    }

    @Test
    void validate_validTokenProvided_returnsUserDetailsAndOkStatus() throws Exception {
        //Arrange
        ValidationResponse validationResponse = ValidationResponse.builder()
                                                                  .username("bob")
                                                                  .role("USER")
                                                                  .build();
        doReturn(validationResponse).when(service)
                                    .getValidationResponse();

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/authentications/validate")
                                                                       .header("Authorizations", "Bearer token"));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(validationResponse)));
    }
}

