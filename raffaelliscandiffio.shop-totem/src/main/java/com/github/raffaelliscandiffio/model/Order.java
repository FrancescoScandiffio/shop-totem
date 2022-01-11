package com.github.raffaelliscandiffio.model;

import java.util.List;

public class Order {

	private List<OrderItem> items;

	public Order(List<OrderItem> items) {
		this.items = items;
	}

	/**
	 * Encapsulate the specified product and quantity in a new OrderItem instance. 
	 * If the product is already binded to an item, increase its quantity.
	 * @param product the product to be added to the order
	 * @param the quantity of product to be added to the order
	 * @throws NullPointerException if product is null
	 * @throws IllegalArgumentException if quantity is non-positive
	 */
	public void insertItem(Product product, int quantity) throws NullPointerException, IllegalArgumentException {
		OrderItem item = items.stream().filter(obj -> obj.getProduct().getId() == product.getId())
				.findFirst()
				.orElse(null);
		if (item == null)
			items.add(new OrderItem(product, quantity));
		else
			item.increaseQuantity(quantity);
	}


}
