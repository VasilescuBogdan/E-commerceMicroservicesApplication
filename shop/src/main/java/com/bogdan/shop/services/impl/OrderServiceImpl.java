package com.bogdan.shop.services.impl;

import com.bogdan.shop.controllers.models.CreateOrderDto;
import com.bogdan.shop.controllers.models.GetOrderDto;
import com.bogdan.shop.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.persistence.entities.Order;
import com.bogdan.shop.persistence.entities.OrderStatus;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.OrderRepository;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    private final ProductRepository productRepository;

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
                             .orderStatus(OrderStatus.IN_PROGRESS)
                             .paymentMethod(null)
                             .user(user)
                             .address(createOrderDto.address())
                             .products(createOrderDto.productIds()
                                                     .stream()
                                                     .map(this::getProduct)
                                                     .toList())
                             .build());
    }

    private GetOrderDto mapOrderToGetOrderDto(Order order) {
        return GetOrderDto.builder()
                          .orderStatus(order.getOrderStatus())
                          .id(order.getId())
                          .user(order.getUser())
                          .address(order.getAddress())
                          .paymentMethod(order.getPaymentMethod())
                          .build();
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceDoesNotExistException("Product does" + " not exist"));
    }
}
