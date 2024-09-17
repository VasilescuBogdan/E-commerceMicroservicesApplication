package com.bogdan.user.integration;

import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.controllers.models.ValidationResponse;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.entities.enums.Role;
import com.bogdan.user.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerIntTest extends IntTest {

    @Autowired
    private UserRepository userRepository;

    private final String baseUrl = "http://localhost:" + port + "/api/authentications";

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
        List<User> updatedList = userRepository.findAll();
        User savedUser = updatedList.get(updatedList.size() - 1);
        assertThat(savedUser.getUsername()).isEqualTo(registerRequest.username());
        assertThat(encoder.matches(registerRequest.password(), savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
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
        List<User> updatedList = userRepository.findAll();
        User savedUser = updatedList.get(updatedList.size() - 1);
        assertThat(savedUser.getUsername()).isEqualTo(registerRequest.username());
        assertThat(encoder.matches(registerRequest.password(), savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
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
