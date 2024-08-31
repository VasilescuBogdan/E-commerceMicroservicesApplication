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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        ResultActions response = mvc.perform(MockMvcRequestBuilders.get("/api/users"));

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
        ResultActions response = mvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId));

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
    void getUser_ServiceThrowsUserNotFoundException_returnStatusBadRequest() throws Exception {
        //Arrange
        long userId = 2;
        Mockito.doThrow(UserNotFoundException.class)
               .when(service)
               .getUser(userId);

        //Act
        ResultActions response = mvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isNotFound());
    }

    @Test
    void deleteUser_ReturnStatusNoContent() throws Exception {
        //Arrange
        long userId = 2;

        //Act
        ResultActions response = mvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}", userId));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isNoContent());
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
        ResultActions response = mvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", userId)
                                                                   .contentType(MediaType.APPLICATION_JSON)
                                                                   .content(objectMapper.writeValueAsString(user)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isNoContent());
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
        ResultActions response = mvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", userId)
                                                                   .contentType(MediaType.APPLICATION_JSON)
                                                                   .content(objectMapper.writeValueAsString(user)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status()
                                                .isNotFound());
    }
}