package org.yearup.models;

import jakarta.persistence.*;

@Entity
@Table(name = "order_line_items")
public class OrderLineItem
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_line_item_id")
    private int orderLineItemId;

    @Column(name = "order_id")
    private int orderId;

    @Column(name = "product_id")
    private int productId;

    @Column(name = "sales_price")
    private double salesPrice;

    private int quantity;
    private double discount;

    // Getters

    public int getOrderLineItemId() { return orderLineItemId; }

    public int getOrderId() { return orderId; }

    public int getProductId() { return productId; }

    public double getSalesPrice() { return salesPrice; }

    public int getQuantity() { return quantity; }

    public double getDiscount() { return discount; }

    // Setters

    public void setOrderLineItemId(int orderLineItemId) { this.orderLineItemId = orderLineItemId; }

    public void setOrderId(int orderId) { this.orderId = orderId; }

    public void setProductId(int productId) { this.productId = productId; }

    public void setSalesPrice(double salesPrice) { this.salesPrice = salesPrice; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void setDiscount(double discount) { this.discount = discount; }
}
