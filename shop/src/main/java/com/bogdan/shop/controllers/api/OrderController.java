package com.bogdan.shop.controllers.api;

import com.bogdan.shop.controllers.models.CreateOrder;
import com.bogdan.shop.controllers.models.GetOrder;
import com.bogdan.shop.controllers.models.UpdateOrder;
import com.bogdan.shop.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    public void createOrder(Principal principal, @RequestBody CreateOrder products) {
        service.createOrder(principal.getName(), products);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    public List<GetOrder> getOrders(Principal principal) {
        return service.getOrdersUser(principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<GetOrder> getOrders() {
        return service.getOrders();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable("id") long orderId, Principal principal) {
        service.deleteOrder(orderId, principal.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateOrder(@PathVariable("id") long orderId, @RequestBody UpdateOrder order, Principal principal) {
        service.updaterOrder(orderId, order, principal.getName());
    }

    @GetMapping("/place/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    public void placeOrder(@PathVariable("id") Long orderId) {
        service.placeOrder(orderId);
    }

    @PatchMapping("/finish/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void finishOrder(@PathVariable("id") Long orderId) {
        service.finishOrder(orderId);
    }
}
