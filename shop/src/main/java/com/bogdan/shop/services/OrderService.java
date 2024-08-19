package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateOrderDto;
import com.bogdan.shop.controllers.models.GetOrderDto;
import com.bogdan.shop.controllers.models.UpdateOrderDto;

import java.util.List;

public interface OrderService {
    List<GetOrderDto> getOrders();

    List<GetOrderDto> getOrdersUser(String user);

    void deleteOrder(long orderId, String user);

    void updaterOrder(long orderId, UpdateOrderDto updateOrderDto, String user);

    void createOrder(String user, CreateOrderDto createOrderDto);

    void placeOrder(Long orderId);

    void finishOrder(Long orderId);
}
