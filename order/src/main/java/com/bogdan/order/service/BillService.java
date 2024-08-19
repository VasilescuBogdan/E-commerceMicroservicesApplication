package com.bogdan.order.service;

import com.bogdan.order.controller.model.GetBillDto;
import com.bogdan.order.integration.messages.model.OrderDetails;

import java.util.List;

public interface BillService {

    void createBill(OrderDetails orderDetails);

    List<GetBillDto> getBillsUser(String user);

    List<GetBillDto> getBills();

    void payBill(Long billId);
}
