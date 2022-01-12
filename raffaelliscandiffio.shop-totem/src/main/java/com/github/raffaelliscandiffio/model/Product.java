package com.github.raffaelliscandiffio.model;

public class Product {

	private final String name;
	private final double price;
	private long id;
	private static long lastId = 0;

	
	/**
	 * Constructs a new Product with the specified name, price and quantity available. Sets its id to a positive incremental number.
	 * @param name of the product
	 * @param price of the product
	 * @throws IllegalArgumentException if the specified name is empty or null or price is negative
	 */
	public Product(String name, double price) {

		if (!(name != null && !name.trim().isEmpty())) {
			throw new IllegalArgumentException("Null or empty name is not allowed");
		}
		this.name = name;

		if (price < 0) {
			throw new IllegalArgumentException("Negative price: " + price);
		}
		this.price = price;
		
		this.id = ++lastId;
	}



	/**
	 * Returns the price
	 * @return the price
	 */
	public double getPrice() {
		return price;
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

	// used for test only
	Product() {
		this.name = "";
		this.price = 1.0;
	}


}