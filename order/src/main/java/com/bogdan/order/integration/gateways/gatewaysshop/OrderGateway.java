package com.bogdan.order.integration.gateways.gatewaysshop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class OrderGateway {

    @Value("${shop-service.url}")
    private String shopServiceURI;

    private final RestClient restClient;

    public OrderGateway(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(shopServiceURI)
                                 .build();
    }

    public void setOrderToFinished(Long orderId) {
        restClient.patch()
                  .uri(shopServiceURI + "/api/orders/finish/{id}", orderId)
                  .retrieve()
                  .toBodilessEntity();
    }
}
