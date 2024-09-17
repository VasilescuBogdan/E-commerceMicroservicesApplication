package com.bogdan.shop.integration;

import com.bogdan.shop.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.shop.integration.gateways.model.ValidationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.mockito.Mockito.doReturn;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public abstract class IntTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.0.1");

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected ObjectMapper mapper;

    @MockBean
    private AuthenticationGateway gateway;

    @LocalServerPort
    protected int port;

    protected String generateTokenUser(String username) {
        return generateTestToken(username, "USER");
    }

    protected String generateTokenAdmin() {
        return generateTestToken("admin", "ADMIN");
    }

    private String generateTestToken(String username, String role) {
        ValidationResponse validationResponse = new ValidationResponse(role, username);
        String testToken = "token";
        doReturn(Optional.of(validationResponse)).when(gateway)
                                                 .validateToken(testToken);
        return "Bearer " + testToken;
    }
}
