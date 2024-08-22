package com.bogdan.user.cotrollers;

import com.bogdan.user.config.SecurityConfig;
import com.bogdan.user.controllers.api.AuthenticationController;
import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
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

    @Autowired
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
    @WithAnonymousUser
    void registerNewUser_registerRequestProvided_isCreated() throws Exception {
        //Arrange
        RegisterRequest request = RegisterRequest.builder()
                                                 .username("alen")
                                                 .password("pass")
                                                 .build();

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/authentications/registerUser")
                                                                       .contentType(MediaType.APPLICATION_JSON)
                                                                       .content(objectMapper.writeValueAsString(
                                                                               request)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isCreated());
    }

    @Test
    @WithAnonymousUser
    void registerNewAdmin_registerRequestProvided_isCreated() throws Exception {
        //Arrange
        RegisterRequest request = RegisterRequest.builder()
                                                 .username("alen")
                                                 .password("pass")
                                                 .build();

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/authentications/registerAdmin")
                                                                       .contentType(MediaType.APPLICATION_JSON)
                                                                       .content(objectMapper.writeValueAsString(
                                                                               request)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isCreated());
    }

    @Test
    @WithAnonymousUser
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
                                                .value(loginResponse.token()));
    }
}
