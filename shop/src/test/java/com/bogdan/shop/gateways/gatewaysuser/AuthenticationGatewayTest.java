package com.bogdan.shop.gateways.gatewaysuser;

import com.bogdan.shop.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.shop.integration.gateways.model.ValidationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

@RestClientTest(AuthenticationGateway.class)
class AuthenticationGatewayTest {

    @Autowired
    MockRestServiceServer server;

    @Autowired
    AuthenticationGateway gateway;

    @Autowired
    ObjectMapper mapper;

    private static final String BASE_URL = "http://localhost:8081/api/authentications";

    @Test
    void validateToken_whenGivenValidToken_returnValidationResponse() throws JsonProcessingException {
        //Arrange
        String token = "token";
        ValidationResponse validationResponse = new ValidationResponse("USER", "user");
        server.expect(requestTo(BASE_URL + "/validate"))
              .andRespond(withSuccess(mapper.writeValueAsString(validationResponse), MediaType.APPLICATION_JSON));

        //Act
        Optional<ValidationResponse> response = gateway.validateToken(token);

        //Assert
        assertThat(response).isNotEmpty()
                            .hasValue(validationResponse);
    }

    @Test
    void validateToken_whenGivenInvalidToken_returnNothing() {
        //Arrange
        String token = "token";
        server.expect(requestTo(BASE_URL + "/validate"))
              .andRespond(withUnauthorizedRequest());

        //Act
        Optional<ValidationResponse> response = gateway.validateToken(token);

        //Assert
        assertThat(response).isEmpty();
    }
}
