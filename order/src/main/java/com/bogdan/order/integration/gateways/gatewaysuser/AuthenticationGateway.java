package com.bogdan.order.integration.gateways.gatewaysuser;

import com.bogdan.order.integration.gateways.model.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationGateway {

    @Value("${user-service.url}")
    private String userServiceURL;

    private final WebClient webClient;

    public Optional<ValidationResponse> validateToken(String token) {
        return webClient.get()
                        .uri(userServiceURL + "/api/authentications/validate")
                        .headers(headers -> headers.add("Authorization", "Bearer " + token))
                        .retrieve()
                        .bodyToMono(ValidationResponse.class)
                        .blockOptional();
    }
}
