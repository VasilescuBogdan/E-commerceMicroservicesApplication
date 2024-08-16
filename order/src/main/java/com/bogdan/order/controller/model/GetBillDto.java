package com.bogdan.order.controller.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetBillDto(String user, LocalDateTime dateTime, Long orderNumber, List<ItemDto> items, Float total) {
}
