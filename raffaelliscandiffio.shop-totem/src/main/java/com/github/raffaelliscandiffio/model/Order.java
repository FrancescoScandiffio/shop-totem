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
		OrderItem storedItem = validateInputsAndFindItemByProduct(product, quantity);
		if (storedItem != null)
			throw new IllegalArgumentException(
					String.format("Product with id %s already exists in this Order", product.getId()));
		storedItem = new OrderItem(product, quantity, product.getPrice() * quantity);
		items.add(storedItem);
		return storedItem;
	}

	public OrderItem increaseProductQuantity(Product product, int quantity) {
		OrderItem storedItem = validateInputsAndFindItemByProduct(product, quantity);
		handleProductNotFound(product, storedItem);
		storedItem.setQuantity(quantity + storedItem.getQuantity());
		updateItemSubtotal(storedItem);
		return storedItem;
	}

	public OrderItem decreaseProductQuantity(Product product, int quantity) {
		OrderItem storedItem = validateInputsAndFindItemByProduct(product, quantity);
		handleProductNotFound(product, storedItem);
		int storedQuantity = storedItem.getQuantity();
		if (quantity >= storedQuantity)
			throw new IllegalArgumentException(
					String.format("Quantity must be less than %s. Received: %s", storedQuantity, quantity));
		storedItem.setQuantity(storedQuantity - quantity);
		updateItemSubtotal(storedItem);
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

	private void handleNullProduct(Product product) {
		if (product == null)
			throw new NullPointerException("Product cannot be null");
	}

	private void handleNotPositiveQuantity(int quantity) {
		if (quantity <= 0)
			throw new IllegalArgumentException(String.format("Quantity must be positive. Received: %s", quantity));
	}

	private OrderItem getFirstItemByProductIdOrNull(Product product) {
		return items.stream().filter(obj -> obj.getProduct().getId() == product.getId()).findFirst().orElse(null);
	}

	private OrderItem validateInputsAndFindItemByProduct(Product product, int quantity) {
		handleNullProduct(product);
		handleNotPositiveQuantity(quantity);
		return getFirstItemByProductIdOrNull(product);
	}

	private void handleProductNotFound(Product product, OrderItem storedItem) {
		if (storedItem == null)
			throw new NoSuchElementException(
					String.format("Product with id %s not found in this Order", product.getId()));
	}

	private void updateItemSubtotal(OrderItem storedItem) {
		storedItem.setSubTotal(storedItem.getQuantity() * storedItem.getProduct().getPrice());
	}

}
