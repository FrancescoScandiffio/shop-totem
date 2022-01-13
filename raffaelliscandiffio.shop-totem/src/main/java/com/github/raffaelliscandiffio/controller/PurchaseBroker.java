package com.github.raffaelliscandiffio.controller;

import java.util.List;

import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;

public class PurchaseBroker {

	List<Product> allProducts;

	public List<Product> getAllProducts() {
		return allProducts;
	}

	public void returnItems(List<OrderItem> orderItems) {
		// TODO implementation required. Now empty because mocked
	}

}
