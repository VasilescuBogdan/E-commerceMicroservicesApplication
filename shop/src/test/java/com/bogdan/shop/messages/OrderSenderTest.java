package com.bogdan.shop.messages;

import com.bogdan.shop.integration.messages.model.OrderItem;
import com.bogdan.shop.integration.messages.model.OrderDetails;
import com.bogdan.shop.integration.messages.sender.OrderSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderSenderTest {

    @Mock
    private RabbitTemplate template;

    @InjectMocks
    private OrderSender sender;

    @Value("${rabbitmq.name.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Test
    void sendOrderMessage() {
        //Arrange
        OrderItem orderItem = new OrderItem("product", 15F);
        OrderDetails message = new OrderDetails("user", "address", List.of(orderItem), 1L);

        //Act
        sender.sendOrderMessage(message);

        //Assert
        verify(template, times(1)).convertAndSend(exchange, routingKey, message);
    }
}
