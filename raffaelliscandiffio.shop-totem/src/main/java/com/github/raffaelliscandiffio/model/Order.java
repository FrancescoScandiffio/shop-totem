package com.github.raffaelliscandiffio.model;

import java.util.List;

public class Order {

	private List<OrderItem> items;

	public Order(List<OrderItem> items) {
		this.items = items;
	}

	public void insertItem(Product product, int quantity) throws NullPointerException {
		int index = 0;
		while (index < items.size() && items.get(index).getProduct().getId() != product.getId())
			index++;

		if (index == items.size())
			items.add(new OrderItem(product, quantity));
		else
			items.get(index).increaseQuantity(quantity);
	}

}
