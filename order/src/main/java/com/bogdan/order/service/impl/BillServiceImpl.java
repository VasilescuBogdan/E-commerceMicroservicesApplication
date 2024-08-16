package com.bogdan.order.service.impl;

import com.bogdan.order.controller.model.GetBillDto;
import com.bogdan.order.controller.model.ItemDto;
import com.bogdan.order.integration.messages.model.OrderDetails;
import com.bogdan.order.persistence.entities.Bill;
import com.bogdan.order.persistence.entities.Item;
import com.bogdan.order.persistence.repositories.BillRepository;
import com.bogdan.order.persistence.repositories.ItemRepository;
import com.bogdan.order.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository repository;

    private final ItemRepository itemRepository;

    @Override
    public void addBill(OrderDetails orderDetails) {
        repository.save(Bill.builder()
                            .orderNumber(orderDetails.orderNumber())
                            .user(orderDetails.user())
                            .dateTime(LocalDateTime.now())
                            .items(orderDetails.orderItem()
                                               .entrySet()
                                               .stream()
                                               .map(entry -> itemRepository.save(
                                                       new Item(null, entry.getKey(), entry.getValue())))
                                               .toList())
                            .build());
    }

    @Override
    public List<GetBillDto> getBillsUser(String user) {
        return repository.findByUser(user)
                         .stream()
                         .map(this::mapBillToGetBillDto)
                         .toList();
    }

    @Override
    public List<GetBillDto> getBills() {
        return repository.findAll()
                         .stream()
                         .map(this::mapBillToGetBillDto)
                         .toList();
    }

    private GetBillDto mapBillToGetBillDto(Bill bill) {
        return GetBillDto.builder()
                         .user(bill.getUser())
                         .dateTime(bill.getDateTime())
                         .orderNumber(bill.getOrderNumber())
                         .items(bill.getItems()
                                    .stream()
                                    .map(item -> ItemDto.builder()
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
