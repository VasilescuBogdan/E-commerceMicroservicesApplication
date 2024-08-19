package com.bogdan.shop.integration.messages.sender;

import com.bogdan.shop.integration.messages.model.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderSender {

    @Value("${rabbitmq.name.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderMessage(OrderMessage orderMessage) {
        log.info("Bill details were sent: {}", orderMessage);
        rabbitTemplate.convertAndSend(exchange, routingKey, orderMessage);
    }
}
