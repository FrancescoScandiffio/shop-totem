package com.github.raffaelliscandiffio.model;

public class Product {

	private final String name;
	private final double price;
	private int availableQuantity;

	public Product(String name, double price, int availableQuantity) {

		if (!(name != null && !name.trim().isEmpty())) {
			throw new IllegalArgumentException("Null or empty name is not allowed");
		}
		this.name = name;

		if (price < 0) {
			throw new IllegalArgumentException("Negative price: " + price);
		}
		this.price = price;

		setAvailableQuantityValidation(availableQuantity);
	}

	private void setAvailableQuantityValidation(int availableQuantity) {
		if (availableQuantity < 0) {
			throw new IllegalArgumentException("Negative available quantity: " + availableQuantity);
		}
		this.availableQuantity = availableQuantity;
	}


	public double getPrice() {
		return price;
	}

	public int getAvailableQuantity() {
		return availableQuantity;
	}

	public String getName() {
		return name;
	}

	
	public void setAvailableQuantity(int availableQuantity) {
		setAvailableQuantityValidation(availableQuantity);
	}

	// used for test only
	Product() {
		this.name = "";
		this.price = 1.0;
	}

	// used for test only
	void initAvailableQuantity(int availableQuantity) {
		this.availableQuantity = availableQuantity;
	}

}