package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.GetOrder;
import com.bogdan.shop.controllers.models.GetProduct;
import com.bogdan.shop.controllers.models.UpdateOrder;
import com.bogdan.shop.persistence.entities.Order;
import com.bogdan.shop.persistence.entities.OrderStatus;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.OrderRepository;
import com.bogdan.shop.services.impl.OrderServiceImpl;
import com.bogdan.shop.util.exceptions.OperationNotSupportedException;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.util.exceptions.ResourceNotOwnedException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl service;

    @Test
    void getOrders_repositoryReturnsAllOrders_returnAllOrdersWithStatusCreated() {
        //Arrange
        Product product1 = new Product(1L, "product1", 30.5F, "this is product 1", null, null);
        Product product2 = new Product(2L, "product2", 50F, "this is product 2", null, null);
        Order order1 = new Order(1L, "user1", "address1", OrderStatus.CREATED, List.of(product1, product1, product2));
        Order order2 = new Order(2L, "user2", "address2", OrderStatus.IN_PROGRESS, List.of(product2));
        Order order3 = new Order(3L, "user1", "address1", OrderStatus.FINISHED, List.of(product1));
        Mockito.when(orderRepository.findAll())
               .thenReturn(List.of(order1, order2, order3));

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
        Product product1 = new Product(1L, "product1", 30.5F, "this is product 1", null, null);
        Product product2 = new Product(2L, "product2", 50F, "this is product 2", null, null);
        Order order1 = new Order(1L, "user1", "address1", OrderStatus.CREATED, List.of(product1, product1, product2));
        Order order2 = new Order(3L, "user1", "address1", OrderStatus.FINISHED, List.of(product1));
        Mockito.when(orderRepository.findByUser("user1"))
               .thenReturn(List.of(order1, order2));

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
    void deleteOrder_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> service.deleteOrder(orderId, "user"))
                  .withMessage("Order with id " + orderId + " does not exist");
    }

    @Test
    void deleteOrder_repositoryReturnsOrderNotOwnedByUser_throwResourceNotOwnException() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(1L, "user1", "address1", OrderStatus.CREATED, null);
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.of(order));

        //Assert
        Assertions.assertThatExceptionOfType(ResourceNotOwnedException.class)
                  .isThrownBy(() -> service.deleteOrder(orderId, "user"))
                  .withMessage("This is not your order!");
    }

    @Test
    void deleteOrder_repositoryReturnsOrderWithStatusNotCreated_throwOperationNotSupportedException() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(1L, "user", "address1", OrderStatus.IN_PROGRESS, null);
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.of(order));

        //Assert
        Assertions.assertThatExceptionOfType(OperationNotSupportedException.class)
                  .isThrownBy(() -> service.deleteOrder(orderId, "user"))
                  .withMessage("Don't have permission to modify this resource!");
    }

    @Test
    void updateOrder_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("address2", List.of(1L, 3L));
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> service.updateOrder(orderId, updateOrder, "user"))
                  .withMessage("Order with id " + orderId + " does not exist");
    }

    @Test
    void updateOrder_repositoryReturnsOrderNotOwnedByUser_throwResourceNotOwnException() {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("address2", List.of(1L, 3L));
        Order order = new Order(1L, "user1", "address1", OrderStatus.CREATED, null);
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.of(order));

        //Assert
        Assertions.assertThatExceptionOfType(ResourceNotOwnedException.class)
                  .isThrownBy(() -> service.updateOrder(orderId, updateOrder, "user"))
                  .withMessage("This is not your order!");
    }

    @Test
    void updateOrder_repositoryReturnsOrderWithStatusNotCreated_throwOperationNotSupportedException() {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("address2", List.of(1L, 3L));
        Order order = new Order(1L, "user", "address1", OrderStatus.IN_PROGRESS, null);
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.of(order));

        //Assert
        Assertions.assertThatExceptionOfType(OperationNotSupportedException.class)
                  .isThrownBy(() -> service.updateOrder(orderId, updateOrder, "user"))
                  .withMessage("Don't have permission to modify this resource!");
    }

    @Test
    void placeOrder_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> service.placeOrder(orderId, "user"))
                  .withMessage("Order with id " + orderId + " does not exist");
    }

    @Test
    void placeOrder_repositoryReturnsOrderNotOwnedByUser_throwResourceNotOwnException() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(1L, "user1", "address1", OrderStatus.CREATED, null);
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.of(order));

        //Assert
        Assertions.assertThatExceptionOfType(ResourceNotOwnedException.class)
                  .isThrownBy(() -> service.placeOrder(orderId, "user"))
                  .withMessage("This is not your order!");
    }

    @Test
    void placeOrder_repositoryReturnsOrderWithStatusNotCreated_throwOperationNotSupportedException() {
        //Arrange
        long orderId = 1L;
        Order order = new Order(1L, "user", "address1", OrderStatus.IN_PROGRESS, null);
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.of(order));

        //Assert
        Assertions.assertThatExceptionOfType(OperationNotSupportedException.class)
                  .isThrownBy(() -> service.placeOrder(orderId, "user"))
                  .withMessage("Don't have permission to modify this resource!");
    }

    @Test
    void finishOrder_repositoryReturnsNothing_throwResourceDoesNotExistException() {
        //Arrange
        long orderId = 1L;
        Mockito.when(orderRepository.findById(orderId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> service.finishOrder(orderId))
                  .withMessage("Order with id " + orderId + " does not exist");
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
