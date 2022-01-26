package com.github.raffaelliscandiffio.model;

import java.util.Objects;

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
	
	/**
	 * Sets quantity
	 * @param quantity available in stock
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * Returns the quantity currently in stock
	 * @return quantity
	 */
	public int getQuantity() {
		return quantity;
	}
	
	/**
	 * Returns the id of the Stock
	 * @return the id
	 */
	public long getId() {
		return id;
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
}
