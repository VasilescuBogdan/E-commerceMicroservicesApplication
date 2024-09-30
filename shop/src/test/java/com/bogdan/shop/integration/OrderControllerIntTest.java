package com.bogdan.shop.integration;

import com.bogdan.shop.controllers.models.CreateOrder;
import com.bogdan.shop.controllers.models.GetOrder;
import com.bogdan.shop.controllers.models.GetProduct;
import com.bogdan.shop.controllers.models.UpdateOrder;
import com.bogdan.shop.integration.messages.model.OrderDetails;
import com.bogdan.shop.integration.messages.model.OrderItem;
import com.bogdan.shop.persistence.entities.Order;
import com.bogdan.shop.persistence.entities.OrderStatus;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.OrderRepository;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIntTest extends IntTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13.6-management");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String baseUrl = "http://localhost:" + port + "/api/orders";

    private final List<Order> orders = new ArrayList<>();

    @Value("${rabbitmq.name.queue}")
    private String queueName;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(null, "product", 25F, "this is product", new ArrayList<>(), new ArrayList<>());
        Order order1 = new Order(null, "user1", "address1", OrderStatus.CREATED, new ArrayList<>());
        Order order2 = new Order(null, "user1", "address2", OrderStatus.FINISHED, new ArrayList<>());
        Order order3 = new Order(null, "user2", "address3", OrderStatus.IN_PROGRESS, new ArrayList<>());
        product.getOrders()
               .addAll(List.of(order1, order2, order3));
        product = productRepository.save(product);
        order1.getProducts()
              .add(product);
        order2.getProducts()
              .add(product);
        order3.getProducts()
              .add(product);
        orders.addAll(List.of(order1, order2, order3));
        orderRepository.saveAll(orders);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE `order` AUTO_INCREMENT=1")
                     .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE product AUTO_INCREMENT=1")
                     .executeUpdate();
    }

    @Test
    void createOrder_responseStatusCreatedAndOrderIsCreated() throws Exception {
        //Arrange
        String user = "user";
        CreateOrder createOrder = new CreateOrder("address 4", List.of(product.getId()));

        //Act
        ResultActions response = mvc.perform(post(baseUrl).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user))
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(mapper.writeValueAsString(createOrder)));

        //Assert
        response.andExpect(status().isCreated());
        List<Order> updatedList = orderRepository.findAll();
        assertThat(updatedList).hasSize(orders.size() + 1);
        Order insertedOrder = updatedList.get(updatedList.size() - 1);
        assertThat(insertedOrder.getUser()).isEqualTo(user);
        assertThat(insertedOrder.getAddress()).isEqualTo(createOrder.address());
        assertThat(insertedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(insertedOrder.getProducts()).containsExactlyInAnyOrderElementsOf(List.of(product));
    }

    @Test
    void getOrdersUser_getOrderList() throws Exception {
        //Arrange
        String user = "user1";
        List<GetOrder> orderList = Stream.of(orders.get(0), orders.get(1))
                                         .map(this::mapOrderToGetOrder)
                                         .toList();

        //Act
        ResultActions response = mvc.perform(
                get(baseUrl + "/user").header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(orderList)));
    }

    @Test
    void getOrdersAdmin_getOrderList() throws Exception {
        //Arrange
        List<GetOrder> getOrderList = Stream.of(orders.get(1), orders.get(2))
                                            .map(this::mapOrderToGetOrder)
                                            .toList();

        //Act
        ResultActions response = mvc.perform(get(baseUrl).header(HttpHeaders.AUTHORIZATION, generateTokenAdmin()));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(getOrderList)));
    }

    @Test
    void deleteOrder_repositoryReturnsActualOrder_responseStatusNoContentAndOrderIsDeleted() throws Exception {
        //Arrange
        String user = "user1";
        long orderId = 1L;

        //Act
        ResultActions response = mvc.perform(
                delete(baseUrl + "/{id}", orderId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isNoContent());
        Optional<Order> isOrder = orderRepository.findById(orderId);
        assertThat(isOrder).isEmpty();
    }

    @Test
    void deleteOrder_repositoryReturnsNothing_responseStatusNotFound() throws Exception {
        //Arrange
        String user = "user1";
        long orderId = 99L;

        //Act
        ResultActions response = mvc.perform(
                delete(baseUrl + "/{id}", orderId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_repositoryReturnsOrderNotOwnedByUser_responseStatusBadRequest() throws Exception {
        //Arrange
        String user = "random user";
        long orderId = 1L;

        //Act
        ResultActions response = mvc.perform(
                delete(baseUrl + "/{id}", orderId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void deleteOrder_repositoryReturnsOrderWithStatusNotCreated_responseStatusBadRequest() throws Exception {
        //Arrange
        String user = "user1";
        long orderId = 2L;

        //Act
        ResultActions response = mvc.perform(
                delete(baseUrl + "/{id}", orderId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void updateOrder_repositoryReturnsActualOrder_responseStatusNoContentAndOrderWasUpdated() throws Exception {
        //Arrange
        long orderId = 1L;
        String user = "user1";
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(product.getId(), product.getId()));

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", orderId).header(HttpHeaders.AUTHORIZATION,
                                                                                    generateTokenUser(user))
                                                                            .contentType(MediaType.APPLICATION_JSON)
                                                                            .content(mapper.writeValueAsString(
                                                                                    updateOrder)));

        //Assert
        response.andExpect(status().isNoContent());
        Order order = orderRepository.getReferenceById(orderId);
        assertThat(order.getProducts()).containsExactlyInAnyOrderElementsOf(List.of(product, product));
        assertThat(order.getAddress()).isEqualTo(updateOrder.address());
    }

    @Test
    void updateOrder_repositoryReturnsNothing_responseStatusNotFound() throws Exception {
        //Arrange
        long orderId = 99L;
        String user = "user1";
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(product.getId(), product.getId()));

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", orderId).header(HttpHeaders.AUTHORIZATION,
                                                                                    generateTokenUser(user))
                                                                            .contentType(MediaType.APPLICATION_JSON)
                                                                            .content(mapper.writeValueAsString(
                                                                                    updateOrder)));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void updateOrder_repositoryReturnsOrderNotOwnedByUser_responseStatusBadRequest() throws Exception {
        //Arrange
        long orderId = 1L;
        String user = "another user";
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(product.getId(), product.getId()));

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", orderId).header(HttpHeaders.AUTHORIZATION,
                                                                                    generateTokenUser(user))
                                                                            .contentType(MediaType.APPLICATION_JSON)
                                                                            .content(mapper.writeValueAsString(
                                                                                    updateOrder)));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void updateOrder_repositoryReturnsOrderStatusNotCreated_responseStatusBadRequest() throws Exception {
        //Arrange
        long orderId = 2L;
        String user = "user1";
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(product.getId(), product.getId()));

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", orderId).header(HttpHeaders.AUTHORIZATION,
                                                                                    generateTokenUser(user))
                                                                            .contentType(MediaType.APPLICATION_JSON)
                                                                            .content(mapper.writeValueAsString(
                                                                                    updateOrder)));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_repositoryReturnsOrder_responseStatusOkAndBillDetailsAreSendToQueue() throws Exception {
        //Arrange
        long orderId = 1L;
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(
                patch(baseUrl + "/place/{id}", orderId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isOk());
        Order placedOrder = orderRepository.getReferenceById(orderId);
        assertThat(placedOrder.getOrderStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        OrderDetails orderDetails = (OrderDetails) rabbitTemplate.receiveAndConvert(queueName);
        assertThat(orderDetails).isNotNull();
        assertThat(orderDetails.orderNumber()).isEqualTo(placedOrder.getId());
        assertThat(orderDetails.user()).isEqualTo(placedOrder.getUser());
        assertThat(orderDetails.address()).isEqualTo(placedOrder.getAddress());
        assertThat(orderDetails.orderItem()).containsExactlyInAnyOrderElementsOf(
                List.of(new OrderItem(product.getName(), product.getPrice())));
    }

    @Test
    void placeOrder_repositoryReturnsNothing_responseStatusNotFound() throws Exception {
        //Arrange
        long orderId = 99L;
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(
                patch(baseUrl + "/place/{id}", orderId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void placeOrder_repositoryReturnsOrderNotOwnedByUser_responseStatusBadRequest() throws Exception {
        //Arrange
        long orderId = 1L;
        String user = "random user";

        //Act
        ResultActions response = mvc.perform(
                patch(baseUrl + "/place/{id}", orderId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_repositoryReturnsOrderWithStatusNotCreated_responseStatusBadRequest() throws Exception {
        //Arrange
        long orderId = 2L;
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(
                patch(baseUrl + "/place/{id}", orderId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void finishOrder_repositoryReturnsOrder_responseStatusOkAndOrderStatusIsFinished() throws Exception {
        //Arrange
        long orderId = 3L;

        //Act
        ResultActions response = mvc.perform(patch(baseUrl + "/finish/{id}", orderId));

        //Assert
        response.andExpect(status().isOk());
        Order finishedOrder = orderRepository.getReferenceById(orderId);
        assertThat(finishedOrder.getOrderStatus()).isEqualTo(OrderStatus.FINISHED);
    }


    @Test
    void finishOrder_repositoryReturnsNothing_responseStatusNotFound() throws Exception {
        //Arrange
        long orderId = 99L;

        //Act
        ResultActions response = mvc.perform(patch(baseUrl + "/finish/{id}", orderId));

        //Assert
        response.andExpect(status().isNotFound());
    }

    private GetOrder mapOrderToGetOrder(Order order) {
        return GetOrder.builder()
                       .id(order.getId())
                       .orderStatus(order.getOrderStatus())
                       .user(order.getUser())
                       .address(order.getAddress())
                       .items(order.getProducts()
                                   .stream()
                                   .map(getProduct -> GetProduct.builder()
                                                                .name(getProduct.getName())
                                                                .price(getProduct.getPrice())
                                                                .description(getProduct.getDescription())
                                                                .build())
                                   .toList())
                       .build();
    }
}
