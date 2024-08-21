package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateOrder;
import com.bogdan.shop.controllers.models.GetOrder;
import com.bogdan.shop.controllers.models.UpdateOrder;

import java.util.List;

public interface OrderService {
    List<GetOrder> getOrders();

    List<GetOrder> getOrdersUser(String user);

    void deleteOrder(long orderId, String user);

    void updaterOrder(long orderId, UpdateOrder updateOrder, String user);

    void createOrder(String user, CreateOrder createOrder);

    void placeOrder(Long orderId);

    void finishOrder(Long orderId);
}
