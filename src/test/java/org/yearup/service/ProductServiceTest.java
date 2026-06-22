package org.yearup.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import org.yearup.models.Product;
import org.yearup.repository.ProductRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Sql(scripts = "classpath:test-insert-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductServiceTest
{
    @Autowired
    private ProductRepository productRepository;

    @Test
    public void search_shouldReturn_AllProducts()
    {
        // arrange
        ProductService productService = new ProductService(productRepository);

        // act
        List<Product> products = productService.search(null, null, null, null);

        // assert
        assertEquals(12, products.size(),
                "Because search should return all 12 products, not just the featured ones.");
    }
}
