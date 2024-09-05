package com.bogdan.order.integration.gateways.gatewaysuser;

import com.bogdan.order.integration.gateways.model.ValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class AuthenticationGateway {

    @Value("${user-service.url}")
    private String userServiceURL;

    private final RestClient restClient;

    public AuthenticationGateway(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(userServiceURL)
                                 .build();
    }

    public Optional<ValidationResponse> validateToken(String token) {
        try {
            return restClient.get()
                             .uri(userServiceURL + "/api/authentications/validate")
                             .headers(headers -> headers.add("Authorization", "Bearer " + token))
                             .retrieve()
                             .body(new ParameterizedTypeReference<>() {
                             });
        } catch (HttpClientErrorException.Unauthorized e) {
            return Optional.empty();
        }
    }
}
