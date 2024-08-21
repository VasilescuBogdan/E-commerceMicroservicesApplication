package com.bogdan.order.service;

import com.bogdan.order.controller.model.GetBill;
import com.bogdan.order.integration.messages.model.OrderDetails;

import java.util.List;

public interface BillService {

    void createBill(OrderDetails orderDetails);

    List<GetBill> getBillsUser(String user);

    List<GetBill> getBills();

    void payBill(Long billId);
}
