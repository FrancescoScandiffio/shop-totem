package com.github.raffaelliscandiffio.controller;


import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;

public class PurchaseBroker {
	
	List<Product> allProducts;

	public List<Product> getAllProducts() {
		return allProducts;
	}

	public void returnProducts(List<OrderItem> orderProducts) {}
	
	public Map.Entry<Product,Integer> buy(long productId, int quantity) {
		return new AbstractMap.SimpleEntry<>(null, 0) ;
	}
}
