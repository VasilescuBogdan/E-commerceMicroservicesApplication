package com.bogdan.order.integration.gateways.gatewaysshop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderGateway {

    @Value("${shop-service.url}")
    private String shopServiceURL;

    private static final String ORDERS_PATH = "/api/orders";

    private final RestTemplate restTemplate = new RestTemplate();

    public void setOrderToFinished(Long orderId) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        restTemplate.exchange(shopServiceURL + ORDERS_PATH + "/finish/" + orderId, HttpMethod.GET, entity, Void.class);
    }
}
