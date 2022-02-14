package com.github.raffaelliscandiffio.model;

import java.util.List;
import java.util.NoSuchElementException;

import com.github.raffaelliscandiffio.utils.ExcludeGeneratedFromCoverage;

public class Order {

	private List<OrderItem> items;

	public Order(List<OrderItem> items) {
		this.items = items;
	}

	public OrderItem addNewProduct(Product product, int quantity) {
		if (product == null)
			throw new NullPointerException("Product cannot be null");
		if (quantity <= 0)
			throw new IllegalArgumentException(String.format("Quantity must be positive. Received: %s", quantity));
		OrderItem storedItem = items.stream().filter(obj -> obj.getProduct().getId() == product.getId()).findFirst()
				.orElse(null);
		if (storedItem != null)
			throw new IllegalArgumentException(
					String.format("Product with id %s already exists in this Order", product.getId()));
		storedItem = new OrderItem(product, quantity, product.getPrice() * quantity);
		items.add(storedItem);
		return storedItem;
	}

	public OrderItem insertItem(Product product, int quantity) {
		if (product == null)
			throw new NullPointerException("Product cannot be null");
		if (quantity <= 0)
			throw new IllegalArgumentException(String.format("Quantity must be positive. Received: %s", quantity));

		OrderItem storedItem = items.stream().filter(obj -> obj.getProduct().getId() == product.getId()).findFirst()
				.orElse(null);

		storedItem.setQuantity(quantity + storedItem.getQuantity());
		storedItem.setSubTotal(storedItem.getQuantity() * storedItem.getProduct().getPrice());
		return storedItem;
	}

	/**
	 * Remove the specified item from order
	 * 
	 * @param itemId the id of the item to be removed
	 * @throws NoSuchElementException if the requested item is not found
	 * @return OrderItem - the removed item
	 */
	public OrderItem popItemById(String itemId) {
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
	public OrderItem decreaseItem(String itemId, int quantity) throws IllegalArgumentException {
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

	private OrderItem findItemById(String itemId) {
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
