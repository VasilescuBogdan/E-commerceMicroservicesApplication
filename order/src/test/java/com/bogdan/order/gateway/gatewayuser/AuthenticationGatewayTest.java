package com.bogdan.order.gateway.gatewayuser;

import com.bogdan.order.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.order.integration.gateways.model.ValidationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationGatewayTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AuthenticationGateway authenticationGateway;

    @Value("${user-service.url}")
    private String userServiceURL;

    @Test
    void validateToken_ShouldReturnValidationResponse_WhenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        ValidationResponse mockResponse = new ValidationResponse("USER", "user");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ValidationResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Optional<ValidationResponse> result = authenticationGateway.validateToken(token);

        // Assert
        assertEquals(Optional.of(mockResponse), result);
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(userServiceURL + "/api/authentications/validate");
        verify(requestHeadersSpec).headers(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(ValidationResponse.class);
    }

    @Test
    void validateToken_ShouldReturnEmptyOptional_WhenTokenIsInvalid() {
        // Arrange
        String token = "invalid-token";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ValidationResponse.class)).thenReturn(Mono.empty());

        // Act
        Optional<ValidationResponse> result = authenticationGateway.validateToken(token);

        // Assert
        assertEquals(Optional.empty(), result);
    }
}
