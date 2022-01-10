package com.github.raffaelliscandiffio.model;

import java.util.List;

public class Order {

	private List<OrderItem> items;

	public Order(List<OrderItem> items) {
		this.items = items;
	}

	public void insertItem(Product product, int quantity) throws NullPointerException {
		OrderItem item = items.stream()
				.filter(obj -> obj.getProduct().getId() == product.getId())
				.findFirst()
				.orElse(null);
		if (item == null)
			items.add(new OrderItem(product, quantity));
		else
			item.increaseQuantity(quantity);
	}

}
