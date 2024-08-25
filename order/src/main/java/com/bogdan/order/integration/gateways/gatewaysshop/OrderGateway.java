package com.bogdan.order.integration.gateways.gatewaysshop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderGateway {

    @Value("${shop-service.url}")
    private String shopServiceURL;

    private final WebClient webClient;

    public void setOrderToFinished(Long orderId) {
        webClient.patch()
                 .uri(shopServiceURL + "/api/orders/finish/{id}", orderId)
                 .retrieve()
                 .toBodilessEntity()
                 .block();
    }
}
