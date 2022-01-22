package com.github.raffaelliscandiffio.model;

public class Stock {
	
	private final Product product;
	private int availableQuantity;
	
	/**
	 * Constructs a new Stock with a product and relative available quantity.
	 * @param product object
	 * @param availableQuantity of the product
	 * @throws NullPointerException if the specified product is null
	 * @throws IllegalArgumentException if the quantity is negative 
	 */
	public Stock(Product product, int availableQuantity) {

		if (product==null) {
			throw new NullPointerException("Null product");
		}
		this.product=product;

		setAvailableQuantityValidation(availableQuantity);
	}

	private void setAvailableQuantityValidation(int availableQuantity) {
		if (availableQuantity < 0) {
			throw new IllegalArgumentException("Negative available quantity: " + availableQuantity);
		}
		this.availableQuantity = availableQuantity;
	}

	/**
	 * Returns the available quantity
	 * @return the available quantity
	 */
	public int getAvailableQuantity() {
		return availableQuantity;
	}
	
	/**
	 * Returns the product
	 * @return the product
	 */
	public Product getProduct() {
		return product;
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
	void initAvailableQuantity(int availableQuantity) {
		this.availableQuantity = availableQuantity;
	}
	
	// used for test only
	Stock() {
		this.product = null;
	}

}
