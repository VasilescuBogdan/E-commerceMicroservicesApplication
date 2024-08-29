package com.bogdan.order.services;

import com.bogdan.order.controller.model.GetBill;
import com.bogdan.order.controller.model.GetItem;
import com.bogdan.order.persistence.entities.Bill;
import com.bogdan.order.persistence.entities.Item;
import com.bogdan.order.persistence.repositories.BillRepository;
import com.bogdan.order.service.impl.BillServiceImpl;
import com.bogdan.order.utils.exception.ResourceDoesNotExistException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillServiceImpl service;

    @Test
    void getBills_repositoryReturnsBillList_returnList() {
        //Arrange
        Bill bill1 = new Bill(1L, "user1", LocalDateTime.of(2000, 10, 11, 10, 22, 33), 3L,
                List.of(new Item(1L, "mare", 13.5F), new Item(2L, "mere", 5F)));
        Bill bill2 = new Bill(1L, "user2", LocalDateTime.of(2015, 12, 10, 12, 0, 3), 3L,
                List.of(new Item(1L, "mare", 13.5F)));
        Mockito.when(billRepository.findAll())
               .thenReturn(List.of(bill1, bill2));

        //Act
        List<GetBill> actualBills = service.getBills();

        //Assert
        Assertions.assertThat(actualBills)
                  .hasSize(2)
                  .isNotNull();
        Assertions.assertThat(actualBills.get(0))
                  .isEqualTo(mapBillToGetBill(bill1));
        Assertions.assertThat(actualBills.get(1))
                  .isEqualTo(mapBillToGetBill(bill2));
    }

    @Test
    void getBillsUser_repositoryReturnsBillList_returnList() {
        //Arrange
        Bill bill1 = new Bill(1L, "user1", LocalDateTime.of(2000, 10, 11, 10, 22, 33), 3L,
                List.of(new Item(1L, "mare", 13.5F), new Item(2L, "mere", 5F)));
        Mockito.when(billRepository.findByUser("user1"))
               .thenReturn(List.of(bill1));

        //Act
        List<GetBill> actualBills = service.getBillsUser("user1");

        //Assert
        Assertions.assertThat(actualBills)
                  .hasSize(1)
                  .isNotNull();
        Assertions.assertThat(actualBills.get(0))
                  .isEqualTo(mapBillToGetBill(bill1));
    }

    @Test
    void payBill_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long billId = 1L;
        Mockito.when(billRepository.findById(billId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> service.payBill(billId))
                  .withMessage("Bill with id " + billId + " does not exist!");
    }

    private GetBill mapBillToGetBill(Bill bill) {
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
