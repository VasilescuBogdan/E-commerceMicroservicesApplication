package com.bogdan.user.controllers;

import com.bogdan.user.controllers.api.AuthenticationController;
import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.service.AuthenticationService;
import com.bogdan.user.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        response.andExpect(MockMvcResultMatchers.status()
                                                .isCreated());
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
        response.andExpect(MockMvcResultMatchers.status()
                                                .isCreated());
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

        doThrow(BadCredentialsException.class).when(service)
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
    @WithMockUser(username = "bob", roles = "USER")
    void validate_validTokenProvided_returnsUserDetailsAndOkStatus() throws Exception {
        //Arrange
        ValidationResponse validationResponse = ValidationResponse.builder()
                                                                  .username("bob")
                                                                  .role("USER")
                                                                  .build();
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        doReturn(auth).when(securityContext)
                      .getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        doReturn(validationResponse).when(service)
                                    .getValidationResponse();

        //Act
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/api/authentications/validate")
                                                                   .header("Authorizations", "Bearer token"))
                                    .andReturn();
        int responseStatus = response.getResponse()
                                     .getStatus();
        String responseBody = response.getResponse()
                                      .getContentAsString();

        //Assert
        Assertions.assertThat(responseStatus)
                  .isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(objectMapper.readValue(responseBody, ValidationResponse.class))
                  .isEqualTo(validationResponse);
    }
}

