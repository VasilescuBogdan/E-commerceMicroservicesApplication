package com.bogdan.order.service.impl;

import com.bogdan.order.controller.model.GetBill;
import com.bogdan.order.controller.model.GetItem;
import com.bogdan.order.integration.gateways.gatewaysshop.OrderGateway;
import com.bogdan.order.integration.messages.model.OrderDetails;
import com.bogdan.order.persistence.entities.Bill;
import com.bogdan.order.persistence.entities.Item;
import com.bogdan.order.persistence.repositories.BillRepository;
import com.bogdan.order.persistence.repositories.ItemRepository;
import com.bogdan.order.service.BillService;
import com.bogdan.order.utils.exception.ResourceDoesNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository repository;

    private final ItemRepository itemRepository;

    private final OrderGateway orderGateway;

    private final BillRepository billRepository;

    @Override
    public void createBill(OrderDetails orderDetails) {
        repository.save(Bill.builder()
                            .orderNumber(orderDetails.orderNumber())
                            .user(orderDetails.user())
                            .dateTime(LocalDateTime.now())
                            .items(orderDetails.orderItem()
                                               .stream()
                                               .map(entry -> itemRepository.save(
                                                       new Item(null, entry.name(), entry.price())))
                                               .toList())
                            .build());
    }

    @Override
    public List<GetBill> getBillsUser(String user) {
        return repository.findByUser(user)
                         .stream()
                         .map(this::mapBillToGetBillDto)
                         .toList();
    }

    @Override
    public List<GetBill> getBills() {
        return repository.findAll()
                         .stream()
                         .map(this::mapBillToGetBillDto)
                         .toList();
    }

    @Override
    public void payBill(Long billId) {
        Bill bill = billRepository.findById(billId)
                                  .orElseThrow(() -> new ResourceDoesNotExistException("Bill does not exist!"));

        // handle payment logic (methodology to be determined)
        orderGateway.setOrderToFinished(bill.getOrderNumber());
    }

    private GetBill mapBillToGetBillDto(Bill bill) {
        return GetBill.builder()
                      .user(bill.getUser())
                      .dateTime(bill.getDateTime())
                      .orderNumber(bill.getOrderNumber())
                      .items(bill.getItems()
                                    .stream()
                                    .map(item -> GetItem.builder()
                                                        .name(item.getName())
                                                        .price(item.getPrice())
                                                        .build())
                                    .toList())
                      .total((float) bill.getItems()
                                            .stream()
                                            .mapToDouble(Item::getPrice)
                                            .sum())
                      .build();
    }
}
