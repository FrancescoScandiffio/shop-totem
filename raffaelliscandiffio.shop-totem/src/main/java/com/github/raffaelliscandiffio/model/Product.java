package com.github.raffaelliscandiffio.model;

public class Product {

	private final String name;
	private final double price;
	private int availableQuantity;
	private long id;
	private static long lastId = 0;

	
	/**
	 * Constructs a new Product with the specified name, price and quantity available. Sets its id to a positive incremental number.
	 * @param name of the product
	 * @param price of the product
	 * @param availableQuantity of the product
	 * @throws IllegalArgumentException if the specified name is empty or null, price is negative and quantity is negative 
	 */
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
		
		this.id = ++lastId;
	}

	
	private void setAvailableQuantityValidation(int availableQuantity) {
		if (availableQuantity < 0) {
			throw new IllegalArgumentException("Negative available quantity: " + availableQuantity);
		}
		this.availableQuantity = availableQuantity;
	}


	/**
	 * Returns the price
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Returns the available quantity
	 * @return the available quantity
	 */
	public int getAvailableQuantity() {
		return availableQuantity;
	}

	/**
	 * Returns the name
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the id
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets available quantity to the specified quantity.
	 * @param availableQuantity to be set
	 * @throws IllegalArgumentException if the specified availableQuantity is negative 
	 */
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