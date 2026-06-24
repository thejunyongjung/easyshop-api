package org.yearup.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.*;
import org.yearup.repository.OrderLineItemRepository;
import org.yearup.repository.OrderRepository;

import java.time.LocalDateTime;

@Service
public class OrderService
{
    private final ShoppingCartService shoppingCartService;
    private final ProfileService profileService;
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;

    public OrderService(ShoppingCartService shoppingCartService,
                        ProfileService profileService,
                        OrderRepository orderRepository,
                        OrderLineItemRepository orderLineItemRepository)
        {
        this.shoppingCartService = shoppingCartService;
        this.profileService = profileService;
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        }

        @Transactional
        public Order checkout(int userId)
        {
            // STEP 1: Load the cart
            ShoppingCart cart = shoppingCartService.getByUserId(userId);

            // can't check out an empty cart - Insomnia 5.7
            if (cart.getItems().isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot checkout an empty cart.");

            // STEP 2: Build & Save the order header
            Profile profile = profileService.getByUserId(userId); // to copy shipping info from the profile

            Order order = new Order();
            order.setUserId(userId);
            order.setDate(LocalDateTime.now()); // timestamp —> The moment of checkout
            order.setAddress(profile.getAddress());
            order.setCity(profile.getCity());
            order.setState(profile.getState());
            order.setZip(profile.getZip());
            order.setShippingAmount(0);

            // before save: order.getOrderId() -> 0
            Order savedOrder = orderRepository.save(order);
            // after save: savedOrder.getOrderId() -> DB-assigned id

            // STEP 3: One line item per cart product
            for (ShoppingCartItem item : cart.getItems().values())
            {
                OrderLineItem lineItem = new OrderLineItem();
                lineItem.setOrderId(savedOrder.getOrderId());
                lineItem.setProductId(item.getProductId());
                lineItem.setSalesPrice(item.getProduct().getPrice());
                lineItem.setQuantity(item.getQuantity());
                lineItem.setDiscount(item.getDiscountPercent());
                orderLineItemRepository.save(lineItem);
            }

            // STEP 4: Clear the cart & return
            shoppingCartService.clear(userId);

            return savedOrder;
        }
}
