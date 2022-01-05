package com.github.raffaelliscandiffio.model;

public class OrderItem {

	private Product product;
	private int quantity;

	public OrderItem(Product product, int quantity) {
		if (product == null)
			throw new NullPointerException("Null product");
		handleNonPositiveQuantity(quantity);
		this.product = product;
		this.quantity = quantity;
	}

	public void increaseQuantity(int amount) {
		handleNonPositiveQuantity(amount);
		this.quantity += amount;
	}

	public void decreaseQuantity(int amount) {
		handleNonPositiveQuantity(amount);
		if (amount >= this.quantity)
			throw new IllegalArgumentException(String.format(
					"Decrease quantity (%d) must be less than available quantity (%d)", amount, this.quantity));
		this.quantity -= amount;
	}

	public int getQuantity() {
		return quantity;
	}

	public Product getProduct() {
		return product;
	}

	private void handleNonPositiveQuantity(int quantity) {
		if (quantity <= 0)
			throw new IllegalArgumentException(String.format("Non-positive quantity: (%d)", quantity));
	}

	// Package-private methods for testing
	OrderItem() {}
	
	void setQuantity(int quantity) {
		this.quantity = quantity;
	}


}
