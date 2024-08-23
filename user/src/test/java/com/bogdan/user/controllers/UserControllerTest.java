package com.bogdan.user.controllers;

import com.bogdan.user.config.SecurityConfig;
import com.bogdan.user.controllers.api.UserController;
import com.bogdan.user.controllers.models.GetUser;
import com.bogdan.user.controllers.models.UpdateUser;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.service.UserService;
import com.bogdan.user.service.impl.JwtServiceImpl;
import com.bogdan.user.utils.enums.Role;
import com.bogdan.user.utils.exceptions.UserNotFoundException;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @MockBean
    private UserService service;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    private String validToken;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                             .alwaysDo(MockMvcResultHandlers.log())
                             .apply(SecurityMockMvcConfigurers.springSecurity())
                             .build();
        User userDetails = new User(null, "user1", "password1", Role.ADMIN);
        validToken = "token";
        Mockito.when(jwtService.isTokenValid(validToken, userDetails))
               .thenReturn(true);
        Mockito.when(jwtService.extractUsername(validToken))
               .thenReturn("user1");
        Mockito.when(userDetailsService.loadUserByUsername("user1"))
               .thenReturn(userDetails);
    }

    @Test
    void getAllUsers_returnStatusOkAndAllUsersList() throws Exception {
        //Arrange
        List<GetUser> users = new ArrayList<>();
        users.add(GetUser.builder()
                         .username("user1")
                         .password("password1")
                         .role(Role.ADMIN)
                         .build());
        users.add(GetUser.builder()
                         .username("user2")
                         .password("password2")
                         .role(Role.USER)
                         .build());
        Mockito.when(service.getAllUsers())
               .thenReturn(users);

        //Act
        ResultActions response = mvc.perform(MockMvcRequestBuilders.get("/api/users")
                                                                   .header("Authorization", "Bearer " + validToken));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username")
                                                .value("user1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].password")
                                                .value("password1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].role")
                                                .value("ADMIN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].username")
                                                .value("user2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].password")
                                                .value("password2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].role")
                                                .value("USER"));
    }

    @Test
    void getUser_returnStatusOkAndUser() throws Exception {
        //Arrange
        long userId = 1;
        GetUser user = GetUser.builder()
                              .username("user2")
                              .password("password2")
                              .role(Role.USER)
                              .build();
        Mockito.when(service.getUser(userId))
               .thenReturn(user);

        //Act
        ResultActions response = mvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId)
                                                                   .header("Authorization", "Bearer " + validToken));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("username")
                                                .value("user2"))
                .andExpect(MockMvcResultMatchers.jsonPath("password")
                                                .value("password2"))
                .andExpect(MockMvcResultMatchers.jsonPath("role")
                                                .value("USER"));
    }

    @Test
    void deleteUser_returnStatusNoContent() throws Exception {
        //Act
        ResultActions response = mvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}", 2)
                                                                   .header("Authorization", "Bearer " + validToken));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isNoContent());
    }

    @Test
    void deleteUser_serviceThrowsUserNotFoundException_returnStatusBadRequest() throws Exception {
        //Arrange
        long userId = 2;
        Mockito.doThrow(UserNotFoundException.class)
               .when(service)
               .deleteUser(userId);

        //Act
        ResultActions response = mvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}", userId)
                                                                   .header("Authorization", "Bearer " + validToken));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isNotFound());
    }

    @Test
    void updateUser_returnStatusNoContent() throws Exception {
        //Arrange
        UpdateUser user = UpdateUser.builder()
                                    .username("user3")
                                    .password("password3")
                                    .build();

        //Act
        ResultActions response = mvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", 1)
                                                                   .header("Authorization", "Bearer " + validToken)
                                                                   .contentType(MediaType.APPLICATION_JSON)
                                                                   .content(objectMapper.writeValueAsString(user)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isNoContent());
    }

    @Test
    void updateUser_serviceThrowsUserNotFoundException_returnStatusBadRequest() throws Exception {
        //Arrange
        long userId = 1;
        UpdateUser user = UpdateUser.builder()
                                    .username("user3")
                                    .password("password3")
                                    .build();
        Mockito.doThrow(UserNotFoundException.class)
               .when(service)
               .updateUser(userId, user);

        //Act
        ResultActions response = mvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", userId)
                                                                   .header("Authorization", "Bearer " + validToken)
                                                                   .contentType(MediaType.APPLICATION_JSON)
                                                                   .content(objectMapper.writeValueAsString(user)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isNotFound());
    }
}