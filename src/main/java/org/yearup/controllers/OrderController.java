package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yearup.models.Order;
import org.yearup.service.OrderService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("orders")
@CrossOrigin
public class OrderController
{
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService)
    {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Order> checkout(Principal principal)
    {
        Order order = orderService.checkout(currentUserId(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /** HELPER METHOD */
    private int currentUserId(Principal principal)
    {
        return userService.getIdByUsername(principal.getName());
    }
}
