package com.bogdan.user.integration;

import com.bogdan.user.controllers.models.LoginRequest;
import com.bogdan.user.controllers.models.LoginResponse;
import com.bogdan.user.controllers.models.RegisterRequest;
import com.bogdan.user.persistence.entities.User;
import com.bogdan.user.persistence.entities.enums.Role;
import com.bogdan.user.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthenticationControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.2");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @LocalServerPort
    private int randomServerPort;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + randomServerPort + "/api/authentications";
        restClient = RestClient.create(baseUrl);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void registerNewUser_responseStatusCreated() {
        //Arrange
        RegisterRequest registerRequest = new RegisterRequest("user", "password");

        //Act
        ResponseEntity<Void> response = restClient.post()
                                                  .uri("/register-user")
                                                  .body(registerRequest)
                                                  .retrieve()
                                                  .toBodilessEntity();

        //Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void registerNewAdmin_responseStatusCreated() {
        //Arrange
        RegisterRequest registerRequest = new RegisterRequest("user", "password");

        //Act
        ResponseEntity<Void> response = restClient.post()
                                                  .uri("/register-admin")
                                                  .body(registerRequest)
                                                  .retrieve()
                                                  .toBodilessEntity();

        //Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void login_authenticationSuccess_responseStatusOkAndReturnLoginResponse() {
        //Arrange
        LoginRequest loginRequest = new LoginRequest("user", "password");
        userRepository.save(
                new User(null, loginRequest.username(), encoder.encode(loginRequest.password()), Role.ADMIN));

        //Act
        ResponseEntity<LoginResponse> response = restClient.post()
                                                           .uri("/login")
                                                           .body(loginRequest)
                                                           .retrieve()
                                                           .toEntity(LoginResponse.class);

        //Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        String jwt = response.getBody().token().substring(7);
        assertThat(jwt).matches("^[\\w-]*\\.[\\w-]*\\.[\\w-]*$");
    }
}
