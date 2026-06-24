package org.yearup.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id") // @Column because filed name differs from DB column name
    private int orderId;

    @Column(name = "user_id")
    private int userId;

    private LocalDateTime date;
    private String address;
    private String city;
    private String state;
    private String zip;

    @Column(name = "shipping_amount")
    private double shippingAmount;

    // Getters

    public int getOrderId() { return orderId; }

    public int getUserId() { return userId; }

    public LocalDateTime getDate() { return date; }

    public String getAddress() { return address; }

    public String getCity() { return city; }

    public String getState() { return state; }

    public String getZip() { return zip; }

    public double getShippingAmount() { return shippingAmount; }

    // Setters

    public void setOrderId(int orderId) { this.orderId = orderId; }

    public void setUserId(int userId) { this.userId = userId; }

    public void setDate(LocalDateTime date) { this.date = date; }

    public void setAddress(String address) { this.address = address; }

    public void setCity(String city) { this.city = city; }

    public void setState(String state) { this.state = state; }

    public void setZip(String zip) { this.zip = zip; }

    public void setShippingAmount(double shippingAmount) { this.shippingAmount = shippingAmount; }
}
