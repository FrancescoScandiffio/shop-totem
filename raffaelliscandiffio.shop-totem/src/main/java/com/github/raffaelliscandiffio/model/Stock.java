package com.github.raffaelliscandiffio.model;

import java.util.Objects;

import com.github.raffaelliscandiffio.utils.ExcludeGeneratedFromCoverage;

public class Stock {

	private final long id;
	private int availableQuantity;
	
	/**
	 * Constructs a new Stock with an id and quantity available.
	 * @param id of the stock
	 * @param availableQuantity of the product
	 * @throws IllegalArgumentException if the quantity is negative 
	 */
	public Stock(long id, int availableQuantity) {

		setAvailableQuantityValidation(availableQuantity);
		this.id = id;
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
	 * Returns the id of the Stock
	 * @return the id
	 */
	@ExcludeGeneratedFromCoverage
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
	
	@Override
	public int hashCode() {
		return Objects.hash(availableQuantity, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		return availableQuantity == other.availableQuantity && id == other.id;
	}


	// used for test only
	void initAvailableQuantity(int availableQuantity) {
		this.availableQuantity = availableQuantity;
	}
	
	// used for test only
	Stock() {
		this.id = 0;
	}
}
