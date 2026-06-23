package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.service.ShoppingCartService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("cart")
@CrossOrigin
// only logged-in users should have access to these actions
public class ShoppingCartController
{
    // a shopping cart controller depends on the service layer
    private final ShoppingCartService shoppingCartService;
    private final UserService userService;

    public ShoppingCartController(ShoppingCartService shoppingCartService, UserService userService)
    {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ShoppingCart getCart(Principal principal)
    {
        return shoppingCartService.getByUserId(currentUserId(principal));
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15  (15 is the productId to be added)
    @PostMapping("products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShoppingCart> add(@PathVariable int productId,
                                            Principal principal)
    {
        ShoppingCart cart = shoppingCartService.add(currentUserId(principal), productId);

        // return the updated cart with status 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15  (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated; return the cart (200 OK)
    @PutMapping("products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ShoppingCart updateQuantity(@PathVariable int productId,
                                        @RequestBody ShoppingCartItem item,
                                        Principal principal)
    {
        ShoppingCart cart = shoppingCartService.updateQuantity(currentUserId(principal), productId, item.getQuantity());
        if (cart == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return cart;
    }

    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart  - return the (now empty) cart so the front end can refresh it (200 OK)
    @DeleteMapping("")
    @PreAuthorize("isAuthenticated()")
    public ShoppingCart clear(Principal principal)
    {
        return shoppingCartService.clear(currentUserId(principal));
    }

    /** HELPER METHOD */
    private int currentUserId(Principal principal)
    {
        return userService.getIdByUsername(principal.getName());
    }
}
