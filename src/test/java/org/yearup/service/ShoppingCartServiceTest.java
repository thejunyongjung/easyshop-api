package org.yearup.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.yearup.models.ShoppingCart;
import org.yearup.repository.ProductRepository;
import org.yearup.repository.ShoppingCartRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Sql(scripts = "classpath:test-insert-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ShoppingCartServiceTest
{
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private ProductRepository productRepository;

    private ShoppingCartService shoppingCartService;
    private final int userId = 1;
    private int productId;

    @BeforeEach
    public void setUp()
    {
        // arrange (shared)
        shoppingCartService = new ShoppingCartService(shoppingCartRepository, new ProductService(productRepository));
        productId = productRepository.findAll().get(0).getProductId();
    }

    @Test
    public void addingNewProduct_shouldStart_atQuantityOne()
    {
        // act - add product to an empty cart
        ShoppingCart cart = shoppingCartService.add(userId, productId);

        // assert1 - one item in the cart
        assertEquals(1, cart.getItems().size(), "Because adding a new product puts one item in the cart");
        // assert2 - its quantity is 1
        assertEquals(1, cart.get(productId).getQuantity(), "Because a newly added product starts at quantity 1");
    }

    @Test
    public void addSameProduct_shouldIncreaseQuantity()
    {
        // act
        shoppingCartService.add(userId, productId);
        ShoppingCart cart = shoppingCartService.add(userId, productId);

        // assert
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.get(productId).getQuantity(), "Because adding the same product again increments to 2");
    }

    @Test
    public void updateQuantity_shouldSet_theExactQuantity()
    {
        // arrange - put the product in the cart first
        shoppingCartService.add(userId, productId);

        // act - example: 6
        ShoppingCart cart = shoppingCartService.updateQuantity(userId, productId, 6);

        // assert
        assertEquals(6, cart.get(productId).getQuantity(), "Because updateQuantity sets the inputted Quantity");
    }

}