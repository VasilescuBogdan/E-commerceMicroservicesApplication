package com.bogdan.shop.controllers.api;

import com.bogdan.shop.controllers.models.CreateOrderDto;
import com.bogdan.shop.controllers.models.GetOrderDto;
import com.bogdan.shop.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public void createOrder(Principal principal, @RequestBody CreateOrderDto products) {
        service.createOrder(principal.getName(), products);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    public List<GetOrderDto> getOrders(Principal principal) {
        return service.getOrder(principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<GetOrderDto> getOrders() {
        return service.getOrder();
    }
}
