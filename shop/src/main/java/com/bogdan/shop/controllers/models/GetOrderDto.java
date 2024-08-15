package com.bogdan.shop.controllers.models;

import com.bogdan.shop.persistence.entities.OrderStatus;
import com.bogdan.shop.persistence.entities.PaymentMethod;
import lombok.Builder;

@Builder
public record GetOrderDto(Long id, String user, String address, OrderStatus orderStatus, PaymentMethod paymentMethod) {
}
