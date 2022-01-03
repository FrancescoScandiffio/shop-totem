package com.github.raffaelliscandiffio.model;

public class Product {

	private final String name;
	private double currentPrice;
	private int availableQuantity;

	public Product(String name, double currentPrice, int availableQuantity) {

		if (!(name != null && !name.trim().isEmpty())) {
			throw new IllegalArgumentException("Null or empty name is not allowed");
		}
		this.name = name;

		setCurrentPriceValidation(currentPrice);

		setAvailableQuantityValidation(availableQuantity);
	}

	private void setAvailableQuantityValidation(int availableQuantity) {
		if (availableQuantity < 0) {
			throw new IllegalArgumentException("Negative available quantity: " + availableQuantity);
		}
		this.availableQuantity = availableQuantity;
	}

	private void setCurrentPriceValidation(double currentPrice) {
		if (currentPrice < 0) {
			throw new IllegalArgumentException("Negative current price: " + currentPrice);
		}
		this.currentPrice = currentPrice;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public int getAvailableQuantity() {
		return availableQuantity;
	}

	public String getName() {
		return name;
	}

	public void setCurrentPrice(double currentPrice) {
		setCurrentPriceValidation(currentPrice);
	}

	public void setAvailableQuantity(int availableQuantity) {
		setAvailableQuantityValidation(availableQuantity);
	}

	// used for test only
	Product() {
		this.name = "";
	}

	// used for test only
	void initProduct(double currentPrice, int availableQuantity) {
		this.currentPrice = currentPrice;
		this.availableQuantity = availableQuantity;
	}

}