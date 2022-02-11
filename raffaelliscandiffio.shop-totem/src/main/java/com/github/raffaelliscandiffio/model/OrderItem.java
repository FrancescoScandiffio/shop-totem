package com.github.raffaelliscandiffio.model;

public class OrderItem {

	private final String id;
	private Product product;
	private int quantity;
	private double subTotal;

	/**
	 * Constructs a new OrderItem with the specified product and quantity.
	 * 
	 * @param product  the product to be purchased
	 * @param quantity the quantity to be purchased
	 * @throws NullPointerException     if the specified product is null
	 * @throws IllegalArgumentException if the specified quantity is non-positive
	 */
	public OrderItem(String id, Product product, int quantity) {
		if (product == null)
			throw new NullPointerException("Null product");
		this.id = id;
		handleNonPositiveQuantity(quantity);
		this.product = product;
		this.quantity = quantity;
		updateSubTotal();
	}

	public String getId() {
		return id;
	}

	public int getQuantity() {
		return quantity;
	}

	public Product getProduct() {
		return product;
	}

	public double getSubTotal() {
		return subTotal;
	}

	private void updateSubTotal() {
		this.subTotal = this.quantity * product.getPrice();
	}

	private void handleNonPositiveQuantity(int quantity) {
		if (quantity <= 0)
			throw new IllegalArgumentException(String.format("Non-positive quantity: (%d)", quantity));
	}

	void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	void setProduct(Product product) {
		this.product = product;
	}

	OrderItem(String id) {
		this.id = id;
	}

}
