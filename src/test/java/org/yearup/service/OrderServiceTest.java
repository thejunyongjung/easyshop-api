package org.yearup.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.ShoppingCart;
import org.yearup.repository.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = "classpath:test-insert-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderServiceTest
{
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderLineItemRepository orderLineItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ShoppingCartRepository shoppingCartRepository;
    @Autowired private ProfileRepository profileRepository;

    private OrderService orderService;
    private ShoppingCartService shoppingCartService;
    private int userId;
    private int productId;

    @BeforeEach
    public void setUp()
    {
        // arrange (shared) - service tree
        shoppingCartService = new ShoppingCartService(shoppingCartRepository, new ProductService(productRepository));
        ProfileService profileService = new ProfileService(profileRepository);
        orderService = new OrderService(shoppingCartService, profileService, orderRepository, orderLineItemRepository);

        userId = profileRepository.findAll().get(0).getUserId();
        productId = productRepository.findAll().get(0).getProductId();
    }

    @Test
    public void checkout_withItemsInCart_shouldCreateOrder_andEmptyTheCart()
    {
        // arrange - put a product in the cart
        shoppingCartService.add(userId, productId);

        // act
         Order order = orderService.checkout(userId);

         // assert
        assertNotNull(order, "Because checkout should create an return an order");
        assertTrue(order.getOrderId() > 0, "Because the saved order should have a DB-generated id");
        assertEquals(1, orderLineItemRepository.findAll().size(), "Because one line item is created per cart product");
        assertTrue(shoppingCartService.getByUserId(userId).getItems().isEmpty(), "Because the cart must be empty after the checkout");
    }

    @Test
    public void checkout_emptyCart_shouldThrow_badRequest()
    {
        // act & assert
        assertThrows(ResponseStatusException.class, () -> orderService.checkout(userId), "Because checking out an empty cart should be rejected");
    }

}
