package com.github.raffaelliscandiffio.model;

public class Product {

	private final String name;
	private final double price;
	private long id;

	/**
	 * Constructs a new Product with the specified id, name, price.
	 * @param id of the product
	 * @param name of the product
	 * @param price of the product
	 * @throws IllegalArgumentException if the specified name is empty or null, price is negative
	 */
	public Product(long id, String name, double price) {
		
		if (!(name != null && !name.trim().isEmpty())) {
			throw new IllegalArgumentException("Null or empty name is not allowed");
		}
		this.name = name;

		if (price < 0) {
			throw new IllegalArgumentException("Negative price: " + price);
		}
		this.price = price;
		
		this.id = id;
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

}