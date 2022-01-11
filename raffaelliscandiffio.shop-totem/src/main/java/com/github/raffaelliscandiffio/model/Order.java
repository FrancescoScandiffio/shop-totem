package com.github.raffaelliscandiffio.model;

import java.util.List;
import java.util.NoSuchElementException;

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
		OrderItem item = items.stream().filter(obj -> obj.getProduct().getId() == product.getId()).findFirst()
				.orElse(null);
		if (item == null)
			items.add(new OrderItem(product, quantity));
		else
			item.increaseQuantity(quantity);
	}

	/**
	 * Remove the specified item from order
	 * @param itemId the id of the item to be removed
	 * @throws NoSuchElementException if the requested item is not found
	 * @return the removed item
	 */
	public OrderItem popItemById(long itemId){
		OrderItem item = findItemById(itemId);
		items.remove(item);
		return item;

	}
	
	public void decreaseItem(long itemId, int quantity) throws IllegalArgumentException {
		OrderItem item = findItemById(itemId);
		item.decreaseQuantity(quantity);

	}

	private OrderItem findItemById(long itemId) {
		return items.stream().filter(obj -> obj.getId() == itemId).findFirst()
				.orElseThrow(() -> new NoSuchElementException(String.format("Item with id (%s) not found", itemId)));
	}

}
