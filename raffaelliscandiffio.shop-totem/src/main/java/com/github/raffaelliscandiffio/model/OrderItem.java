package com.github.raffaelliscandiffio.model;

public class OrderItem {

	private Product product;
	private int quantity;
	private double subTotal;

	/**
	 * Constructs a new OrderItem with the specified product and quantity.
	 * 
	 * @param product the product to be purchased
	 * @param quantity the quantity to be purchased
	 * @throws NullPointerException if the specified product is null
	 * @throws IllegalArgumentException if the specified quantity is non-positive
	 */
	public OrderItem(Product product, int quantity) {
		if (product == null)
			throw new NullPointerException("Null product");
		handleNonPositiveQuantity(quantity);
		this.product = product;
		this.quantity = quantity;
		updateSubTotal();
	}

	/**
	 * Increase the quantity by the specified amount.
	 * 
	 * @param amount the amount to be added to the current quantity
	 * @throws IllegalArgumentException if the specified amount is non-positive
	 */
	public void increaseQuantity(int amount) {
		handleNonPositiveQuantity(amount);
		this.quantity += amount;
		updateSubTotal();
	}

	/**
	 * Decrease the quantity by the specified amount.
	 * 
	 * @param amount the amount to be removed from the current quantity
	 * @throws IllegalArgumentException in the following three cases:
	 *  <ul>
	 *  	<li>if the specified amount is non-positive</li>
	 *  	<li>if the specified amount is equal to the current quantity</li>
	 *  	<li>if the specified amount is greater than the current quantity</li>
	 *  </ul>
	 */
	public void decreaseQuantity(int amount) {
		handleNonPositiveQuantity(amount);
		if (amount >= this.quantity)
			throw new IllegalArgumentException(String
					.format("Decrease quantity (%d) must be less than available quantity (%d)", amount, this.quantity));
		this.quantity -= amount;
		updateSubTotal();

	}

	private void updateSubTotal() {
		this.subTotal = this.quantity * product.getPrice();
	}

	/**
	 * Returns the current quantity
	 * 
	 * @return the current quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Returns the product
	 * 
	 * @return the product
	 */
	public Product getProduct() {
		return product;
	}
	
	/**
	 * Returns the sub total of the item
	 * 
	 * @return subTotal
	 */
	public double getSubTotal() {
		return subTotal;
	}

	private void handleNonPositiveQuantity(int quantity) {
		if (quantity <= 0)
			throw new IllegalArgumentException(String.format("Non-positive quantity: (%d)", quantity));
	}
	
	// Package-private methods for testing
	
	OrderItem(){}

	void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
	
	
	

}
