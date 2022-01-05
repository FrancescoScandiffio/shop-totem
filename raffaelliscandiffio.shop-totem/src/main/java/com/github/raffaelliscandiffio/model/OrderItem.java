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

	public void increaseQuantity(int quantity) {
		handleNonPositiveQuantity(quantity);
		this.quantity += quantity;
	}

	public void decreaseQuantity(int quantity) {
		handleNonPositiveQuantity(quantity);
		if (quantity >= this.quantity)
			throw new IllegalArgumentException(String.format(
					"Decrease quantity (%d) must be less than available quantity (%d)", quantity, this.quantity));
		this.quantity -= quantity;
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

	// Package-private constructor for testing
	OrderItem(int quantity) {
		this.quantity = quantity;
	}


}
