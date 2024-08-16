package com.bogdan.order.service;

import com.bogdan.order.integration.messages.model.OrderDetails;

public interface BillService {

    void addBill(OrderDetails orderDetails);
}
