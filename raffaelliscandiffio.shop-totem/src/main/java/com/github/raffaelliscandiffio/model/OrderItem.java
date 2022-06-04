package com.github.raffaelliscandiffio.model;

import java.util.Objects;

public class OrderItem {

	private Product product;
	private int quantity;
	private double subTotal;

	// JPA uses this to initialise an embedded OrderItem
	@SuppressWarnings("unused")
	private OrderItem() {
	}

	public OrderItem(Product product, int quantity) {
		this.product = product;
		this.quantity = quantity;
		this.subTotal = quantity * product.getPrice();
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

	public Product getProduct() {
		return product;
	}

	@Override
	public int hashCode() {
		return Objects.hash(product);
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
		return Objects.equals(product, other.product);
	}

}
