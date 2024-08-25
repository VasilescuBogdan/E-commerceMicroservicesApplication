package com.bogdan.shop.services.impl;

import com.bogdan.shop.controllers.models.CreateOrder;
import com.bogdan.shop.controllers.models.GetOrder;
import com.bogdan.shop.controllers.models.GetProduct;
import com.bogdan.shop.controllers.models.UpdateOrder;
import com.bogdan.shop.integration.messages.model.OrderItem;
import com.bogdan.shop.integration.messages.model.OrderMessage;
import com.bogdan.shop.integration.messages.sender.OrderSender;
import com.bogdan.shop.util.exceptions.OperationNotSupportedException;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.persistence.entities.Order;
import com.bogdan.shop.persistence.entities.OrderStatus;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.OrderRepository;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.services.OrderService;
import com.bogdan.shop.util.exceptions.ResourceNotOwnedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String RESOURCE_DOES_NOT_EXIST_EXCEPTION_MESSAGE = "Order does not exist";

    private final OrderRepository repository;

    private final ProductRepository productRepository;

    private final OrderSender sender;

    @Override
    public void createOrder(String user, CreateOrder createOrder) {
        repository.save(Order.builder()
                             .orderStatus(OrderStatus.CREATED)
                             .user(user)
                             .address(createOrder.address())
                             .products(createOrder.productIds()
                                                  .stream()
                                                  .map(this::getProduct)
                                                  .toList())
                             .build());
    }

    public List<GetOrder> getOrders() {
        return repository.findAll()
                         .stream()
                         .filter(order -> order.getOrderStatus() != OrderStatus.CREATED)
                         .map(this::mapOrderToGetOrderDto)
                         .toList();
    }

    @Override
    public List<GetOrder> getOrdersUser(String user) {
        return repository.findByUser(user)
                         .stream()
                         .map(this::mapOrderToGetOrderDto)
                         .toList();
    }

    @Override
    public void deleteOrder(long orderId, String user) {
        Order order = getValidOrder(orderId, user);
        repository.delete(order);
    }

    @Override
    public void updaterOrder(long orderId, UpdateOrder updateOrder, String user) {
        Order order = getValidOrder(orderId, user);
        order.setAddress(updateOrder.address());
        order.setProducts(updateOrder.productIds()
                                     .stream()
                                     .map(this::getProduct)
                                     .collect(Collectors.toCollection(ArrayList::new)));
        repository.save(order);
    }

    @Override
    public void placeOrder(Long orderId) {
        Order order = repository.findById(orderId)
                                .orElseThrow(() -> new ResourceDoesNotExistException(
                                        RESOURCE_DOES_NOT_EXIST_EXCEPTION_MESSAGE));
        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new OperationNotSupportedException();
        }
        sender.sendOrderMessage(OrderMessage.builder()
                                            .user(order.getUser())
                                            .address(order.getAddress())
                                            .orderItem(order.getProducts()
                                                            .stream()
                                                            .map(item -> new OrderItem(item.getName(), item.getPrice()))
                                                            .toList())
                                            .orderNumber(order.getId())
                                            .build());
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        repository.save(order);
    }

    @Override
    public void finishOrder(Long orderId) {
        Order order = repository.findById(orderId)
                                .orElseThrow(() -> new ResourceDoesNotExistException(
                                        RESOURCE_DOES_NOT_EXIST_EXCEPTION_MESSAGE));
        order.setOrderStatus(OrderStatus.FINISHED);
        repository.save(order);
    }

    private Order getValidOrder(long orderId, String user) {
        Order order = repository.findById(orderId)
                                .orElseThrow(() -> new ResourceDoesNotExistException(
                                        RESOURCE_DOES_NOT_EXIST_EXCEPTION_MESSAGE));
        if (!Objects.equals(order.getUser(), user)) {
            throw new ResourceNotOwnedException("This is not your order!");
        }
        if (order.getOrderStatus() != OrderStatus.CREATED) {
            throw new OperationNotSupportedException();
        }
        return order;
    }

    private GetOrder mapOrderToGetOrderDto(Order order) {
        return GetOrder.builder()
                       .orderStatus(order.getOrderStatus())
                       .id(order.getId())
                       .user(order.getUser())
                       .address(order.getAddress())
                       .items(order.getProducts()
                                      .stream()
                                      .map(this::mapProductToGetProductDto)
                                      .toList())
                       .build();
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceDoesNotExistException("Product does not exist"));
    }

    private GetProduct mapProductToGetProductDto(Product product) {
        return GetProduct.builder()
                         .name(product.getName())
                         .description(product.getDescription())
                         .price(product.getPrice())
                         .build();
    }
}
