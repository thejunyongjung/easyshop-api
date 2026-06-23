package org.yearup.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import org.yearup.models.CartItem;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.repository.ShoppingCartRepository;

import java.util.List;

@Service
public class ShoppingCartService
{
    // a shopping cart is built from cart rows plus a product lookup for each row
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductService productService;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository, ProductService productService)
    {
        this.shoppingCartRepository = shoppingCartRepository;
        this.productService = productService;
    }

    public ShoppingCart getByUserId(int userId)
    {
        List<CartItem> cartItems = shoppingCartRepository.findByUserId(userId);

        ShoppingCart cart = new ShoppingCart();
        for (CartItem cartItem : cartItems) {
            Product product = productService.getById(cartItem.getProductId());

            ShoppingCartItem item = new ShoppingCartItem();
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            cart.add(item);
        }
        return cart;
    }

    public ShoppingCart add(int userId, int productId)
    {
        CartItem existingItem = shoppingCartRepository.findByUserIdAndProductId(userId, productId);

        if (existingItem == null) {
            // new item addition —> create a new row with quantity 1
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(productId);
            newItem.setQuantity(1);
            shoppingCartRepository.save(newItem);
        } else {
            // if the item is already in the cart —> increment the quantity
            existingItem.setQuantity(existingItem.getQuantity() + 1);
            shoppingCartRepository.save(existingItem);
        }
        return getByUserId(userId);
    }

    public ShoppingCart updateQuantity(int userId, int productId, int quantity)
    {
        CartItem existingItem = shoppingCartRepository.findByUserIdAndProductId(userId, productId);

        // if not in the cart —> controller gives 404
        if (existingItem == null) return null;

        existingItem.setQuantity(quantity);
        shoppingCartRepository.save(existingItem);
        return getByUserId(userId);
    }

    @Transactional
    public ShoppingCart clear(int userId)
    {
        shoppingCartRepository.deleteByUserId(userId);
        return getByUserId(userId);
    }

}
