package com.bogdan.order.gateway.gatewayuser;

import com.bogdan.order.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.order.integration.gateways.model.ValidationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationGatewayTest {

    private MockWebServer webServer;

    private AuthenticationGateway authenticationGateway;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeAll() {
        webServer = new MockWebServer();
        WebClient webClient = WebClient.builder()
                                       .baseUrl(webServer.url("/")
                                                         .toString())
                                       .build();
        authenticationGateway = new AuthenticationGateway(webClient);
    }

    @AfterEach
    void afterAll() throws IOException {
        webServer.close();
    }

    @Test
    void validateToken_whenGivenValidToken_returnValidationResponse() throws JsonProcessingException {
        //Arrange
        String token = "token";
        ValidationResponse validationResponse = new ValidationResponse("USER", "user");
        webServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
                                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                            .setBody(objectMapper.writeValueAsString(validationResponse)));

        //Act
        Optional<ValidationResponse> response = authenticationGateway.validateToken(token);

        //Assert
        assertThat(response).isNotEmpty()
                            .hasValue(validationResponse);
    }

    @Test
    void validateToken_whenGivenInvalidToken_returnNothing() {
        //Arrange
        String token = "token";
        webServer.enqueue(new MockResponse().setResponseCode(HttpStatus.UNAUTHORIZED.value()));

        //Act
        Optional<ValidationResponse> response = authenticationGateway.validateToken(token);

        //Assert
        assertThat(response).isEmpty();
    }
}
