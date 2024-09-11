package com.bogdan.user.integration;

import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.entities.enums.Role;
import com.bogdan.user.persistence.repositories.UserRepository;
import com.bogdan.user.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthenticationControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.0.1");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @LocalServerPort
    private int randomServerPort;

    private final String baseUrl = "http://localhost:" + randomServerPort + "/api/authentications";


    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void registerNewUser_responseStatusCreated() throws Exception {
        //Arrange
        RegisterRequest registerRequest = new RegisterRequest("user", "password");

        //Act
        ResultActions response = mockMvc.perform(post(baseUrl + "/register-user").contentType(
                                                                                         MediaType.APPLICATION_JSON)
                                                                                 .content(mapper.writeValueAsString(
                                                                                         registerRequest)));

        //Assert
        response.andExpect(status().isCreated());
    }

    @Test
    void registerNewAdmin_responseStatusCreated() throws Exception {
        //Arrange
        RegisterRequest registerRequest = new RegisterRequest("user", "password");

        //Act
        ResultActions response = mockMvc.perform(post(baseUrl + "/register-admin").contentType(
                                                                                          MediaType.APPLICATION_JSON)
                                                                                  .content(mapper.writeValueAsString(
                                                                                          registerRequest)));

        //Assert
        response.andExpect(status().isCreated());
    }

    @Test
    void login_authenticationSuccess_responseStatusOkAndReturnLoginResponse() throws Exception {
        //Arrange
        LoginRequest loginRequest = new LoginRequest("user", "password");
        userRepository.save(
                new User(null, loginRequest.username(), encoder.encode(loginRequest.password()), Role.ADMIN));

        //Act
        ResultActions response = mockMvc.perform(post(baseUrl + "/login").contentType(MediaType.APPLICATION_JSON)
                                                                         .content(mapper.writeValueAsString(
                                                                                 loginRequest)));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token", matchesPattern("^[\\w-]*\\.[\\w-]*\\.[\\w-]*$")));
    }

    @Test
    void login_authenticationFailed_responseStatusUnauthenticated() throws Exception {
        //Arrange
        LoginRequest loginRequest = new LoginRequest("user", "password");

        //Act
        ResultActions response = mockMvc.perform(post(baseUrl + "/login").contentType(MediaType.APPLICATION_JSON)
                                                                         .content(mapper.writeValueAsString(
                                                                                 loginRequest)));

        //Assert
        response.andExpect(status().isUnauthorized());
    }

    @Test
    void validateToken_tokenIsValid_returnValidationResponseAndResponseStatusOk() throws Exception {
        //Arrange
        User user = new User(null, "user", "", Role.USER);
        ValidationResponse validationResponse = new ValidationResponse(user.getRole()
                                                                           .name(), user.getUsername());
        String token = jwtService.generateToken(user);
        userRepository.save(user);

        //Act
        ResultActions response = mockMvc.perform(
                get(baseUrl + "/validate").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(validationResponse)));
    }

    @Test
    void validateToken_tokenIdNotValid_responseStatusUnauthorised() throws Exception {
        //Arrange
        String token = jwtService.generateToken(new User(null, "", "", Role.USER));

        //Act
        ResultActions response = mockMvc.perform(
                get(baseUrl + "/validate").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        //Assert
        response.andExpect(status().isForbidden());
    }
}
