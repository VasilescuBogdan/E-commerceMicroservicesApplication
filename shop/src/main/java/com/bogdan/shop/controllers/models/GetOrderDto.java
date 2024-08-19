package com.bogdan.shop.controllers.models;

import com.bogdan.shop.util.enums.OrderStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record GetOrderDto(Long id, String user, String address, OrderStatus orderStatus,
                          List<GetProductDto> items) {
}
