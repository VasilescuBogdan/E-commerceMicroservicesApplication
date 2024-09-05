package com.bogdan.user.controllers;

import com.bogdan.user.controllers.api.UserController;
import com.bogdan.user.controllers.models.GetUser;
import com.bogdan.user.controllers.models.UpdateUser;
import com.bogdan.user.service.JwtService;
import com.bogdan.user.service.UserService;
import com.bogdan.user.persistence.entities.enums.Role;
import com.bogdan.user.utils.exceptions.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockBean
    private UserService service;

    @MockBean
    JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                             .build();
    }

    @Test
    void getAllUsers_serviceReturnsAllUsers_returnStatusOkAndAllUsersList() throws Exception {
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
        doReturn(users).when(service)
                       .getAllUsers();

        //Act
        ResultActions response = mvc.perform(get("/api/users"));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(users)));
    }

    @Test
    void getUser_serviceReturnsUser_returnStatusOkAndUser() throws Exception {
        //Arrange
        long userId = 1;
        GetUser user = GetUser.builder()
                              .username("user2")
                              .password("password2")
                              .role(Role.USER)
                              .build();
        doReturn(user).when(service)
                      .getUser(userId);

        //Act
        ResultActions response = mvc.perform(get("/api/users/{id}", userId));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    void getUser_ServiceThrowsUserNotFoundException_returnStatusBadRequest() throws Exception {
        //Arrange
        long userId = 2;
        Mockito.doThrow(UserNotFoundException.class)
               .when(service)
               .getUser(userId);

        //Act
        ResultActions response = mvc.perform(get("/api/users/{id}", userId));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ReturnStatusNoContent() throws Exception {
        //Arrange
        long userId = 2;

        //Act
        ResultActions response = mvc.perform(delete("/api/users/{id}", userId));

        //Assert
        response.andExpect(status().isNoContent());
        verify(service, times(1)).deleteUser(userId);
    }

    @Test
    void updateUser_serviceReturnsUser_returnStatusNoContent() throws Exception {
        //Arrange
        long userId = 1;
        UpdateUser user = UpdateUser.builder()
                                    .username("user3")
                                    .password("password3")
                                    .build();

        //Act
        ResultActions response = mvc.perform(put("/api/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                                                                           .content(objectMapper.writeValueAsString(
                                                                                   user)));

        //Assert
        response.andExpect(status().isNoContent());
        verify(service, times(1)).updateUser(userId, user);
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
        ResultActions response = mvc.perform(put("/api/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                                                                           .content(objectMapper.writeValueAsString(
                                                                                   user)));

        //Assert
        response.andExpect(status().isNotFound());
    }
}