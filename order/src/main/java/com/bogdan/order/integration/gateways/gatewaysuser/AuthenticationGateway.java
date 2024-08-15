package com.bogdan.order.integration.gateways.gatewaysuser;

import com.bogdan.order.integration.gateways.model.ValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthenticationGateway {

    @Value("${user-service.url}")
    private String userServiceURL;

    private final RestTemplate restTemplate = new RestTemplate();

    public ValidationResponse validateToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<ValidationResponse> entity = new HttpEntity<>(headers);
        ResponseEntity<ValidationResponse> validationResponse = restTemplate.exchange(
                userServiceURL + "/api/authentications/validate", HttpMethod.GET, entity, ValidationResponse.class);
        if (validationResponse.getStatusCode()
                              .is2xxSuccessful()) {
            return validationResponse.getBody();
        }
        return null;
    }
}
