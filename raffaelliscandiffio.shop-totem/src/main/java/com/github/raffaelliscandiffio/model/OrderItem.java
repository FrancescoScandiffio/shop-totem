package com.github.raffaelliscandiffio.model;

import java.util.Objects;

public class OrderItem {

	private final String id;
	private final Product product;
	private int quantity;
	private double subTotal;

	public OrderItem(String id, Product product, int quantity, double subTotal) {
		this.id = id;
		this.product = product;
		this.quantity = quantity;
		this.subTotal = subTotal;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, product, quantity, subTotal);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderItem other = (OrderItem) obj;
		return Objects.equals(id, other.id) && Objects.equals(product, other.product) && quantity == other.quantity
				&& Double.doubleToLongBits(subTotal) == Double.doubleToLongBits(other.subTotal);
	}

	public String getId() {
		return id;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

}
