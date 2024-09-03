package com.bogdan.order.services;

import com.bogdan.order.controller.model.GetBill;
import com.bogdan.order.controller.model.GetItem;
import com.bogdan.order.integration.gateways.gatewaysshop.OrderGateway;
import com.bogdan.order.integration.messages.model.OrderDetails;
import com.bogdan.order.integration.messages.model.OrderItem;
import com.bogdan.order.persistence.entities.Bill;
import com.bogdan.order.persistence.entities.Item;
import com.bogdan.order.persistence.repositories.BillRepository;
import com.bogdan.order.persistence.repositories.ItemRepository;
import com.bogdan.order.service.impl.BillServiceImpl;
import com.bogdan.order.utils.exception.ResourceDoesNotExistException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderGateway orderGateway;

    @InjectMocks
    private BillServiceImpl service;

    @Test
    void createBill_repositoryCallsSaveSuccessfully() {
        //Arrange
        OrderItem orderItem1 = new OrderItem("product1", 10F);
        OrderItem orderItem2 = new OrderItem("product", 15.5F);
        OrderDetails orderDetails = new OrderDetails("user", "address", List.of(orderItem1, orderItem2), 1L);
        Item item1 = new Item(1L, orderItem1.name(), orderItem1.price());
        doReturn(item1).when(itemRepository)
                       .save(new Item(null, orderItem1.name(), orderItem1.price()));
        Item item2 = new Item(2L, orderItem2.name(), orderItem2.price());
        doReturn(item2).when(itemRepository)
                       .save(new Item(null, orderItem2.name(), orderItem2.price()));

        //Act
        service.createBill(orderDetails);

        //Assert
        verify(billRepository, times(1)).save(argThat(bill -> orderDetails.user()
                                                                          .equals(bill.getUser()) &&
                                                              bill.getId() == null && bill.getItems()
                                                                                          .equals(List.of(item1,
                                                                                                  item2)) &&
                                                              bill.getDateTime() != null && bill.getOrderNumber()
                                                                                                .equals(orderDetails.orderNumber())));
    }


    @Test
    void getBills_repositoryReturnsBillList_returnList() {
        //Arrange
        Bill bill1 = new Bill(1L, "user1", LocalDateTime.of(2000, 10, 11, 10, 22, 33), 3L,
                List.of(new Item(1L, "mare", 13.5F), new Item(2L, "mere", 5F)));
        Bill bill2 = new Bill(1L, "user2", LocalDateTime.of(2015, 12, 10, 12, 0, 3), 3L,
                List.of(new Item(1L, "mare", 13.5F)));
        doReturn(List.of(bill1, bill2)).when(billRepository)
                                       .findAll();

        //Act
        List<GetBill> actualBills = service.getBills();

        //Assert
        assertThat(actualBills).hasSize(2)
                               .isNotNull();
        assertThat(actualBills.get(0)).isEqualTo(mapBillToGetBill(bill1));
        assertThat(actualBills.get(1)).isEqualTo(mapBillToGetBill(bill2));
    }

    @Test
    void getBillsUser_repositoryReturnsBillList_returnList() {
        //Arrange
        Bill bill1 = new Bill(1L, "user1", LocalDateTime.of(2000, 10, 11, 10, 22, 33), 3L,
                List.of(new Item(1L, "mare", 13.5F), new Item(2L, "mere", 5F)));
        doReturn(List.of(bill1)).when(billRepository)
                                .findByUser("user1");

        //Act
        List<GetBill> actualBills = service.getBillsUser("user1");

        //Assert
        assertThat(actualBills).hasSize(1)
                               .isNotNull();
        assertThat(actualBills.get(0)).isEqualTo(mapBillToGetBill(bill1));
    }

    @Test
    void payBill_repositoryReturnsBill_orderGatewayIsCalled() {
        //Arrange
        long billId = 1L;
        Bill bill = new Bill(billId, "user", null, 2L, List.of());
        doReturn(Optional.of(bill)).when(billRepository)
                      .findById(billId);

        //Act
        service.payBill(billId);

        //Assert
        verify(orderGateway, times(1)).setOrderToFinished(bill.getOrderNumber());
    }

    @Test
    void payBill_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long billId = 1L;
        doReturn(Optional.empty()).when(billRepository)
                                  .findById(billId);

        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(() -> service.payBill(billId))
                                                                      .withMessage("Bill with id " + billId +
                                                                                   " does not exist!");
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
