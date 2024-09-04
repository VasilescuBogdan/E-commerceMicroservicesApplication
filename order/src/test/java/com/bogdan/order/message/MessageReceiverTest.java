package com.bogdan.order.message;

import com.bogdan.order.integration.messages.model.OrderDetails;
import com.bogdan.order.integration.messages.model.OrderItem;
import com.bogdan.order.integration.messages.receiver.MessageReceiver;
import com.bogdan.order.service.BillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageReceiverTest {

    @Mock
    private BillService billService;

    @InjectMocks
    private MessageReceiver receiver;

    @Test
    void consumeOrderDetails() {
        //Arrange
        OrderItem orderItem = new OrderItem("product", 15F);
        OrderDetails orderDetails = new OrderDetails("user", "address", List.of(orderItem), 1L);

        //Act
        receiver.consumeOrderDetails(orderDetails);

        //Assert
        verify(billService, times(1)).createBill(orderDetails);
    }
}
