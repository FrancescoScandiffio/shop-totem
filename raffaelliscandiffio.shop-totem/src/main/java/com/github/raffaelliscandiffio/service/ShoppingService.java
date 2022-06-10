package com.github.raffaelliscandiffio.service;

import java.util.List;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;

public interface ShoppingService {

	public List<Product> getAllProducts();

	public Order openNewOrder();

	public void deleteOrder(String orderId);

	public void closeOrder(String orderId);

	public void deleteItem(OrderItem orderItem);

	public OrderItem returnItem(OrderItem orderItem, int quantityReturned);

	public List<OrderItem> getOrderItems(String orderId);

	public OrderItem buyProduct(String orderId, String productId, int quantity);

}
