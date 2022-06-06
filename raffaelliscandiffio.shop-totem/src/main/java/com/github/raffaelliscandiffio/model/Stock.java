package com.github.raffaelliscandiffio.model;

import java.util.Objects;

public class Stock {

	private String id;
	private final Product product;
	private int quantity;

	public Stock(Product product, int quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, product, quantity);
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
		return Objects.equals(id, other.id) && Objects.equals(product, other.product) && quantity == other.quantity;
	}

}
