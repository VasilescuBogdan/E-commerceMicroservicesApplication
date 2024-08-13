package com.bogdan.shop.integration.gateways.gatewaysuser;

import com.bogdan.shop.integration.gateways.model.ValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthenticationGateway {

    @Value("${jwt.validation.url}")
    private String validationURL;

    private final RestTemplate restTemplate = new RestTemplate();

    public ValidationResponse validateToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<ValidationResponse> entity = new HttpEntity<>(headers);
        ResponseEntity<ValidationResponse> validationResponse = restTemplate.exchange(validationURL, HttpMethod.GET,
                entity, ValidationResponse.class);
        if (validationResponse.getStatusCode()
                              .is2xxSuccessful()) {
            return validationResponse.getBody();
        }
        return null;
    }
}
