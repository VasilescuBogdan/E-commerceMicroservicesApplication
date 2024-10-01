package com.bogdan.order.integration.messagereceiver;

import com.bogdan.order.integration.messages.model.OrderDetails;
import com.bogdan.order.integration.messages.model.OrderItem;
import com.bogdan.order.integration.messages.receiver.MessageReceiver;
import com.bogdan.order.persistence.entities.Bill;
import com.bogdan.order.persistence.repositories.BillRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
class OrderReceiverIntTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.0.1");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13.6-management");

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private RabbitTemplate template;

    @Value("${rabbitmq.name.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Test
    void consumeOrderDetails_newBillIsCreated() throws InterruptedException {
        // Arrange
        OrderItem item1 = new OrderItem("product", 10F);
        OrderItem item2 = new OrderItem("product", 15F);
        OrderDetails orderDetails = new OrderDetails("user", "address1", List.of(item1, item2), 1L);

        // Act
        template.convertAndSend(exchange, routingKey, orderDetails);
        Thread.sleep(1000);

        // Assert
        Bill savedBill = billRepository.findAll()
                                       .get(0);
        assertThat(savedBill.getUser()).isEqualTo(orderDetails.user());
        assertThat(savedBill.getOrderNumber()).isEqualTo(orderDetails.orderNumber());
        assertThat(savedBill.getItems()
                            .get(0)
                            .getName()).isEqualTo(orderDetails.orderItems()
                                                              .get(0)
                                                              .name());
        assertThat(savedBill.getItems()
                            .get(0)
                            .getPrice()).isEqualTo(orderDetails.orderItems()
                                                               .get(0)
                                                               .price());
        assertThat(savedBill.getItems()
                            .get(1)
                            .getName()).isEqualTo(orderDetails.orderItems()
                                                              .get(1)
                                                              .name());
        assertThat(savedBill.getItems()
                            .get(1)
                            .getPrice()).isEqualTo(orderDetails.orderItems()
                                                               .get(1)
                                                               .price());
    }
}
