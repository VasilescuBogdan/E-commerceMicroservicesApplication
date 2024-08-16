package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateOrderDto;
import com.bogdan.shop.controllers.models.GetOrderDto;

import java.util.List;

public interface OrderService {
    List<GetOrderDto> getOrder();

    List<GetOrderDto> getOrder(String user);

    void createOrder(String user, CreateOrderDto createOrderDto);

    void finalizeOrder(Long orderId);
}
