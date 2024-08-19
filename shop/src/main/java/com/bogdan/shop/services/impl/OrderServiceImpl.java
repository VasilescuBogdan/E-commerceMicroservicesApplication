package com.bogdan.shop.services.impl;

import com.bogdan.shop.controllers.models.CreateOrderDto;
import com.bogdan.shop.controllers.models.GetOrderDto;
import com.bogdan.shop.controllers.models.GetProductReviewDto;
import com.bogdan.shop.integration.messages.model.OrderDetails;
import com.bogdan.shop.integration.messages.sender.MessageSender;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.persistence.entities.Order;
import com.bogdan.shop.persistence.entities.OrderStatus;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.OrderRepository;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    private final ProductRepository productRepository;

    private final MessageSender sender;
    private final OrderRepository orderRepository;

    @Override
    public List<GetOrderDto> getOrder() {
        return repository.findAll()
                         .stream()
                         .map(this::mapOrderToGetOrderDto)
                         .toList();
    }

    @Override
    public List<GetOrderDto> getOrder(String user) {
        return repository.findByUser(user)
                         .stream()
                         .map(this::mapOrderToGetOrderDto)
                         .toList();
    }

    @Override
    public void createOrder(String user, CreateOrderDto createOrderDto) {
        repository.save(Order.builder()
                             .orderStatus(OrderStatus.CREATED)
                             .user(user)
                             .address(createOrderDto.address())
                             .products(createOrderDto.productIds()
                                                     .stream()
                                                     .map(this::getProduct)
                                                     .toList())
                             .build());
    }

    @Override
    public void placeOrder(Long orderId) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        sender.sendOrderDetails(OrderDetails.builder()
                                            .user(order.getUser())
                                            .address(order.getAddress())
                                            .orderItem(order.getProducts()
                                                            .stream()
                                                            .collect(Collectors.toMap(Product::getName,
                                                                    Product::getPrice)))
                                            .orderNumber(order.getId())
                                            .build());
        orderRepository.save(order);
    }

    @Override
    public void setOrderToFinished(Long orderId) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(OrderStatus.FINISHED);
        repository.save(order);
    }

    private GetOrderDto mapOrderToGetOrderDto(Order order) {
        return GetOrderDto.builder()
                          .orderStatus(order.getOrderStatus())
                          .id(order.getId())
                          .user(order.getUser())
                          .address(order.getAddress())
                          .items(order.getProducts()
                                      .stream()
                                      .map(this::mapProductToGetProductReviewDto)
                                      .toList())
                          .build();
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceDoesNotExistException("Product does not exist"));
    }

    private Order getOrderById(Long orderId) {
        return repository.findById(orderId)
                         .orElseThrow(() -> new ResourceDoesNotExistException("Order does not exist"));
    }

    private GetProductReviewDto mapProductToGetProductReviewDto(Product product) {
        return GetProductReviewDto.builder()
                                  .name(product.getName())
                                  .description(product.getDescription())
                                  .price(product.getPrice())
                                  .build();
    }
}
