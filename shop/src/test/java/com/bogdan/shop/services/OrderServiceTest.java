package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateOrder;
import com.bogdan.shop.controllers.models.GetOrder;
import com.bogdan.shop.controllers.models.GetProduct;
import com.bogdan.shop.controllers.models.UpdateOrder;
import com.bogdan.shop.integration.messages.model.OrderDetails;
import com.bogdan.shop.integration.messages.sender.OrderSender;
import com.bogdan.shop.persistence.entities.Order;
import com.bogdan.shop.persistence.entities.OrderStatus;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.OrderRepository;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.services.impl.OrderServiceImpl;
import com.bogdan.shop.util.exceptions.OperationNotSupportedException;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.util.exceptions.ResourceNotOwnedException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderSender sender;

    @InjectMocks
    private OrderServiceImpl service;

    @Test
    void createOrder_productRepositoryReturnsProduct_repositoryCallsSaveMethod() {
        //Arrange
        String user = "user";
        CreateOrder createOrder = new CreateOrder("address", List.of(1L));
        Product product = new Product(createOrder.productIds()
                                                 .get(0), "product", 10F, "this is product", new ArrayList<>());
        doReturn(Optional.of(product)).when(productRepository)
                                      .findById(createOrder.productIds()
                                                           .get(0));
        Order order = new Order(null, user, createOrder.address(), OrderStatus.CREATED, List.of(product));

        //Act
        service.createOrder(user, createOrder);

        //Assert
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void createOrder_productRepositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        String user = "user";
        CreateOrder createOrder = new CreateOrder("address", List.of(1L));
        doReturn(Optional.empty()).when(productRepository)
                                  .findById(createOrder.productIds()
                                                       .get(0));
        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(() -> {
            //Act
            service.createOrder(user, createOrder);
        });
    }

    @Test
    void getOrders_repositoryReturnsAllOrders_returnAllOrdersWithStatusCreated() {
        //Arrange
        Product product1 = new Product(1L, "product1", 30.5F, "this is product 1", new ArrayList<>());
        Product product2 = new Product(2L, "product2", 50F, "this is product 2", new ArrayList<>());
        Order order1 = new Order(1L, "user1", "address1", OrderStatus.CREATED, List.of(product1, product1, product2));
        Order order2 = new Order(2L, "user2", "address2", OrderStatus.IN_PROGRESS, List.of(product2));
        Order order3 = new Order(3L, "user1", "address1", OrderStatus.FINISHED, List.of(product1));
        doReturn(List.of(order1, order2, order3)).when(orderRepository)
                                                 .findAll();

        //Act
        List<GetOrder> actualOrders = service.getOrders();

        //Assert
        Assertions.assertThat(actualOrders)
                  .isNotNull()
                  .hasSize(2);
        Assertions.assertThat(actualOrders.get(0))
                  .isEqualTo(mapOrderToGetOrder(order2));
        Assertions.assertThat(actualOrders.get(1))
                  .isEqualTo(mapOrderToGetOrder(order3));
    }

    @Test
    void getOrdersUser_repositoryReturnsAllOrdersAfterUser_returnAllOrders() {
        //Arrange
        Product product1 = new Product(1L, "product1", 30.5F, "this is product 1", new ArrayList<>());
        Product product2 = new Product(2L, "product2", 50F, "this is product 2", new ArrayList<>());
        Order order1 = new Order(1L, "user1", "address1", OrderStatus.CREATED, List.of(product1, product1, product2));
        Order order2 = new Order(3L, "user1", "address1", OrderStatus.FINISHED, List.of(product1));
        doReturn(List.of(order1, order2)).when(orderRepository)
                                         .findByUser("user1");

        //Act
        List<GetOrder> actualOrders = service.getOrdersUser("user1");

        //Assert
        Assertions.assertThat(actualOrders)
                  .isNotNull()
                  .hasSize(2);
        Assertions.assertThat(actualOrders.get(0))
                  .isEqualTo(mapOrderToGetOrder(order1));
        Assertions.assertThat(actualOrders.get(1))
                  .isEqualTo(mapOrderToGetOrder(order2));
    }

    @Test
    void deleteOrder_repositoryReturnsOrderWithStatusCreated_saveIsCalled() {
        //Arrange
        long orderId = 1L;
        String user = "user";
        Order order = new Order(orderId, user, "address1", OrderStatus.CREATED, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);

        //Act
        service.deleteOrder(orderId, user);

        //Assert
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void deleteOrder_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        doReturn(Optional.empty()).when(orderRepository)
                                  .findById(orderId);

        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(
                                                                              () -> service.deleteOrder(orderId,
                                                                                      "user"))
                                                                      .withMessage("Order with id " + orderId +
                                                                                   " does not exist");
    }

    @Test
    void deleteOrder_repositoryReturnsOrderNotOwnedByUser_throwResourceNotOwnException() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(orderId, "user1", "address1", OrderStatus.CREATED, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);

        //Assert
        assertThatExceptionOfType(ResourceNotOwnedException.class).isThrownBy(
                                                                          () -> service.deleteOrder(orderId, "user"))
                                                                  .withMessage("This is not your order!");
    }

    @Test
    void deleteOrder_repositoryReturnsOrderWithStatusNotCreated_throwOperationNotSupportedException() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(orderId, "user", "address1", OrderStatus.IN_PROGRESS, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);

        //Assert
        assertThatExceptionOfType(OperationNotSupportedException.class).isThrownBy(
                                                                               () -> service.deleteOrder(orderId,
                                                                                       "user"))
                                                                       .withMessage(
                                                                               "Don't have permission to modify this " +
                                                                               "resource!");
    }

    @Test
    void updateOrder_repositoryReturnsOrderWithStatusCreatedAndProductRepositoryReturnsProduct_saveIsCalled() {
        //Arrange
        long orderId = 1L;
        String user = "user";
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(1L));
        Order order = new Order(orderId, user, "address1", OrderStatus.CREATED, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);
        Product product = new Product(updateOrder.productIds()
                                                 .get(0), "product", 10F, "this product", new ArrayList<>());
        doReturn(Optional.of(product)).when(productRepository)
                                      .findById(updateOrder.productIds()
                                                           .get(0));
        Order updatedOrder = new Order(order.getId(), order.getUser(), updateOrder.address(), order.getOrderStatus(),
                List.of(product));

        //Act
        service.updateOrder(orderId, updateOrder, user);

        //Assert
        verify(orderRepository, times(1)).save(updatedOrder);
    }

    @Test
    void updateOrder_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("address2", List.of(1L, 3L));
        doReturn(Optional.empty()).when(orderRepository)
                                  .findById(orderId);

        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(
                                                                              () -> service.updateOrder(orderId,
                                                                                      updateOrder, "user"))
                                                                      .withMessage("Order with id " + orderId +
                                                                                   " does not exist");
    }

    @Test
    void updateOrder_repositoryReturnsOrderNotOwnedByUser_throwResourceNotOwnedException() {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("address2", List.of(1L, 3L));
        Order order = new Order(orderId, "user1", "address1", OrderStatus.CREATED, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);

        //Assert
        assertThatExceptionOfType(ResourceNotOwnedException.class).isThrownBy(() -> {
                                                                      //Act
                                                                      service.updateOrder(orderId, updateOrder, "user");
                                                                  })
                                                                  .withMessage("This is not your order!");
    }

    @Test
    void updateOrder_repositoryReturnsOrderWithStatusNotCreated_throwOperationNotSupportedException() {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("address2", List.of(1L, 3L));
        Order order = new Order(orderId, "user", "address1", OrderStatus.IN_PROGRESS, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);

        //Assert
        assertThatExceptionOfType(OperationNotSupportedException.class).isThrownBy(() -> {
                                                                           //Act
                                                                           service.updateOrder(orderId, updateOrder,
                                                                                   "user");
                                                                       })
                                                                       .withMessage(
                                                                               "Don't have permission to modify this " +
                                                                               "resource!");
    }

    @Test
    void updateOrder_repositoryReturnsOrderWithStatusCreatedButProductRepositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        String user = "user";
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(1L));
        Order order = new Order(orderId, user, "address1", OrderStatus.CREATED, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);
        doReturn(Optional.empty()).when(productRepository)
                                  .findById(updateOrder.productIds()
                                                       .get(0));

        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(() -> {
            //Act
            service.updateOrder(orderId, updateOrder, user);
        });

    }

    @Test
    void placeOrder_repositoryReturnsOrderWithStatusCreated_saveIsCalled() {
        //Arrange
        long orderId = 1L;
        String user = "user";
        Order order = new Order(orderId, user, "address1", OrderStatus.CREATED, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);
        OrderDetails message = new OrderDetails(order.getUser(), order.getAddress(), List.of(), order.getId());
        Order updatedOrder = new Order(order.getId(), order.getUser(), order.getAddress(), OrderStatus.IN_PROGRESS,
                order.getProducts());

        //Act
        service.placeOrder(orderId, user);

        //Assert
        verify(sender, times(1)).sendOrderMessage(message);
        verify(orderRepository, times(1)).save(updatedOrder);
    }

    @Test
    void placeOrder_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        doReturn(Optional.empty()).when(orderRepository)
                                  .findById(orderId);

        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(() -> {
                                                                          //Act
                                                                          service.placeOrder(orderId, "user");
                                                                      })
                                                                      .withMessage("Order with id " + orderId +
                                                                                   " does not exist");
    }

    @Test
    void placeOrder_repositoryReturnsOrderNotOwnedByUser_throwResourceNotOwnedException() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(1L, "user1", "address1", OrderStatus.CREATED, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);

        //Assert
        assertThatExceptionOfType(ResourceNotOwnedException.class).isThrownBy(() -> {
                                                                      //Act
                                                                      service.placeOrder(orderId, "user");
                                                                  })
                                                                  .withMessage("This is not your order!");
    }

    @Test
    void placeOrder_repositoryReturnsOrderWithStatusNotCreated_throwOperationNotSupportedException() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(1L, "user", "address1", OrderStatus.IN_PROGRESS, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);

        //Assert
        assertThatExceptionOfType(OperationNotSupportedException.class).isThrownBy(() -> {
                                                                           //Act
                                                                           service.placeOrder(orderId, "user");
                                                                       })
                                                                       .withMessage(
                                                                               "Don't have permission to modify this " +
                                                                               "resource!");
    }

    @Test
    void finishOrder_repositoryReturnsOrder_saveIsCalled() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(orderId, "user", "address", OrderStatus.IN_PROGRESS, new ArrayList<>());
        doReturn(Optional.of(order)).when(orderRepository)
                                    .findById(orderId);
        Order updatedOrder = new Order(order.getId(), order.getUser(), order.getAddress(), OrderStatus.FINISHED,
                new ArrayList<>());

        //Act
        service.finishOrder(orderId);

        //Assert
        verify(orderRepository, times(1)).save(updatedOrder);
    }

    @Test
    void finishOrder_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        doReturn(Optional.empty()).when(orderRepository)
                                  .findById(orderId);

        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(() -> {
                                                                          //Act
                                                                          service.finishOrder(orderId);
                                                                      })
                                                                      .withMessage("Order with id " + orderId +
                                                                                   " does not exist");
    }

    private GetOrder mapOrderToGetOrder(Order order) {
        return GetOrder.builder()
                       .orderStatus(order.getOrderStatus())
                       .id(order.getId())
                       .user(order.getUser())
                       .address(order.getAddress())
                       .items(order.getProducts()
                                   .stream()
                                   .map(this::mapProductToGetProduct)
                                   .toList())
                       .build();
    }

    private GetProduct mapProductToGetProduct(Product product) {
        return GetProduct.builder()
                         .name(product.getName())
                         .description(product.getDescription())
                         .price(product.getPrice())
                         .build();
    }
}
