package com.github.raffaelliscandiffio.model;

import java.util.Objects;

import com.github.raffaelliscandiffio.utils.ExcludeGeneratedFromCoverage;

public class Stock {

	private final long id;
	private int quantity;
	
	/**
	 * Constructs a new Stock with an id and quantity available.
	 * @param id of the stock
	 * @param quantity of the product currently in stock
	 */
	public Stock(long id, int quantity) {

		this.id = id;
		this.quantity = quantity;
	}

	private void setAvailableQuantityValidation(int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException("Negative quantity: " + quantity);
		}
		this.quantity = quantity;
	}

	/**
	 * Returns the available quantity
	 * @return the available quantity
	 */
	public int getQuantity() {
		return quantity;
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
	 * Sets quantity to the specified quantity.
	 * @param quantity to be set
	 * @throws IllegalArgumentException if the specified quantity is negative 
	 */
	public void setQuantity(int quantity) {
		setAvailableQuantityValidation(quantity);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(quantity, id);
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
		return quantity == other.quantity && id == other.id;
	}


	// used for test only
	void initQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	// used for test only
	Stock() {
		this.id = 0;
	}
}
