package com.github.raffaelliscandiffio.model;

import java.util.Objects;

public class OrderItem {

	private String id;
	private Product product;
	private Order order;
	private int quantity;
	private double subTotal;

	public OrderItem(Product product, Order order, int quantity) {
		this.product = product;
		this.order = order;
		this.quantity = quantity;
		this.subTotal = quantity * product.getPrice();
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
		this.subTotal = quantity * product.getPrice();

	}

	public Product getProduct() {
		return product;
	}

	public Order getOrder() {
		return order;
	}

	public double getSubTotal() {
		return subTotal;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, order, product, quantity);
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
		return Objects.equals(id, other.id) && Objects.equals(order, other.order)
				&& Objects.equals(product, other.product) && quantity == other.quantity;
	}

}
