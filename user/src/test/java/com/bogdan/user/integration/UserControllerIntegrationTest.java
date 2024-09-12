package com.bogdan.user.integration;

import com.bogdan.user.controllers.models.GetUser;
import com.bogdan.user.controllers.models.UpdateUser;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.entities.enums.Role;
import com.bogdan.user.persistence.repositories.UserRepository;
import com.bogdan.user.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.0.1");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EntityManager manager;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:" + port + "/api/users";

    private final List<User> usersList = new ArrayList<>();

    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    void setUp() {
        User user1 = new User(1L, "user1", "password1", Role.USER);
        User user2 = new User(2L, "user2", "password2", Role.USER);
        User user3 = new User(3L, "admin", "password3", Role.ADMIN);
        User user4 = new User(4L, "user3", "password4", Role.USER);
        usersList.addAll(List.of(user1, user2, user3, user4));
        userRepository.saveAll(usersList);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(user3));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        manager.createNativeQuery("ALTER TABLE user AUTO_INCREMENT = 1")
               .executeUpdate();
    }

    @Test
    void getAllUsers_responseStatusOkAndReturnList() throws Exception {
        //Arrange
        List<GetUser> expectedUserList = usersList.stream()
                                                  .map(user -> GetUser.builder()
                                                                      .username(user.getUsername())
                                                                      .password(user.getPassword())
                                                                      .role(user.getRole())
                                                                      .build())
                                                  .toList();

        //Act
        ResultActions response = mockMvc.perform(get(baseUrl).headers(headers));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedUserList)));
    }

    @Test
    void getUser_userIsFound_responseStatusOkAndReturnUser() throws Exception {
        //Arrange
        long userId = 2L;
        GetUser user = GetUser.builder()
                              .username(usersList.get(1)
                                                 .getUsername())
                              .password(usersList.get(1)
                                                 .getPassword())
                              .role(usersList.get(1)
                                             .getRole())
                              .build();

        //Act
        ResultActions response = mockMvc.perform(get(baseUrl + "/{id}", userId).headers(headers));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(user)));
    }

    @Test
    void getUser_userNotFound_responseStatusNotFound() throws Exception {
        //Arrange
        long userId = 99L;

        //Act
        ResultActions response = mockMvc.perform(get(baseUrl + "/{id}", userId).headers(headers));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_responseStatusNoContentAndCheckIfUserIsActualDeleted() throws Exception {
        //Arrange
        long userId = 1L;

        //Act
        ResultActions response = mockMvc.perform(delete(baseUrl + "/{id}", userId).headers(headers));

        //Assert
        response.andExpect(status().isNoContent());
        Optional<User> isUser = userRepository.findById(userId);
        assertThat(isUser).isEmpty();
    }

    @Test
    void updateUser_userIsFound_responseStatusNoContentAndUserIsUpdated() throws Exception {
        //Arrange
        long userId = 1L;
        UpdateUser updateUser = UpdateUser.builder()
                                          .username("new username")
                                          .password("new password")
                                          .build();
        //Act
        ResultActions response = mockMvc.perform(put(baseUrl + "/{id}", userId).headers(headers)
                                                                               .contentType(MediaType.APPLICATION_JSON)
                                                                               .content(mapper.writeValueAsString(
                                                                                       updateUser)));

        //Assert
        response.andExpect(status().isNoContent());
        Optional<User> isUser = userRepository.findById(userId);
        assertThat(isUser).isNotEmpty();
        assertThat(isUser.get().getUsername()).isEqualTo(updateUser.username());
        assertThat(encoder.matches(updateUser.password(), isUser.get().getPassword())).isTrue();
    }

    @Test
    void updateUser_userIsNotFound_responseStatusNotFound() throws Exception {
        //Arrange
        long userId = 99L;
        UpdateUser updateUser = UpdateUser.builder()
                                          .username("new username")
                                          .password("new password")
                                          .build();
        //Act
        ResultActions response = mockMvc.perform(put(baseUrl + "/{id}", userId).headers(headers)
                                                                               .contentType(MediaType.APPLICATION_JSON)
                                                                               .content(mapper.writeValueAsString(
                                                                                       updateUser)));

        //Assert
        response.andExpect(status().isNotFound());
    }
}
