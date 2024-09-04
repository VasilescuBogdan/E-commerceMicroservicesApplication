package com.bogdan.shop.integration.gateways.gatewaysuser;

import com.bogdan.shop.integration.gateways.model.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
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
                        .onErrorResume(WebClientResponseException.class, ex -> {
                            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                return Mono.empty();
                            }
                            return Mono.error(ex);
                        })
                        .blockOptional();
    }
}
