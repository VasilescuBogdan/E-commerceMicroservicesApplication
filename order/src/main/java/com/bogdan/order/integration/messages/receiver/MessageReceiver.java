package com.bogdan.order.integration.messages.receiver;

import com.bogdan.order.integration.messages.model.OrderDetails;
import com.bogdan.order.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageReceiver {

    private final BillService billService;

    @RabbitListener(queues = "${rabbitmq.name.queue}")
    public void consumeOrderDetails(OrderDetails orderDetails) {
        log.info("Details were received: {}", orderDetails);
        billService.createBill(orderDetails);
    }
}
