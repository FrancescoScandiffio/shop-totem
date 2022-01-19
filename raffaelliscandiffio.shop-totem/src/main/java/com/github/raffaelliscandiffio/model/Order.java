package com.github.raffaelliscandiffio.model;

import java.util.List;
import java.util.NoSuchElementException;

import com.github.raffaelliscandiffio.utils.ExcludeGeneratedFromCoverage;

public class Order {

	private List<OrderItem> items;

	public Order(List<OrderItem> items) {
		this.items = items;
	}

	/**
	 * Encapsulate the specified product and quantity in a new OrderItem instance.
	 * If the product is already binded to an item, increase its quantity.
	 * 
	 * @param product the product to be added to the order
	 * @param the     quantity of product to be added to the order
	 * @throws NullPointerException     if product is null
	 * @throws IllegalArgumentException if quantity is non-positive
	 * @return OrderItem - the newly inserted or modified item
	 */
	public OrderItem insertItem(Product product, int quantity) throws NullPointerException, IllegalArgumentException {
		OrderItem item = items.stream().filter(obj -> obj.getProduct().getId() == product.getId()).findFirst()
				.orElse(null);
		if (item == null) {
			item = new OrderItem(product, quantity);
			items.add(item);
		} else
			item.increaseQuantity(quantity);
		return item;
	}

	/**
	 * Remove the specified item from order
	 * 
	 * @param itemId the id of the item to be removed
	 * @throws NoSuchElementException if the requested item is not found
	 * @return OrderItem - the removed item
	 */
	public OrderItem popItemById(long itemId) {
		OrderItem item = findItemById(itemId);
		items.remove(item);
		return item;

	}

	/**
	 * Decrease the quantity of the specified item
	 * 
	 * @param itemId   the item whose quantity has to be decreased
	 * @param quantity the quantity to be removed
	 * @throws IllegalArgumentException in the following cases:
	 *                                  <ul>
	 *                                  <li>if quantity is non-positive</li>
	 *                                  <li>if quantity is equal or greater than the
	 *                                  available quantity</li>
	 * @throws NoSuchElementException   if the requested item is not found
	 * @return OrderItem - the modified item
	 */
	public OrderItem decreaseItem(long itemId, int quantity) throws IllegalArgumentException {
		OrderItem item = findItemById(itemId);
		item.decreaseQuantity(quantity);
		return item;

	}

	/**
	 * Find the item which contains the product matching the specified id. It is
	 * assured that there is only one match or none.
	 * 
	 * @param productId the id of the product
	 * @return The item which contains the specified product, else null
	 */
	public OrderItem findItemByProductId(long productId) {
		return items.stream().filter(obj -> obj.getProduct().getId() == productId).findFirst().orElse(null);

	}

	/**
	 * Removes all the elements from items list. The list will be empty after this
	 * call returns.
	 */
	public void clear() {
		items.clear();
	}

	private OrderItem findItemById(long itemId) {
		return items.stream().filter(obj -> obj.getId() == itemId).findFirst()
				.orElseThrow(() -> new NoSuchElementException(String.format("Item with id (%s) not found", itemId)));
	}

	/**
	 * @return the items
	 */
	@ExcludeGeneratedFromCoverage
	public List<OrderItem> getItems() {
		return items;
	}

}
