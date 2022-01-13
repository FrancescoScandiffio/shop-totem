package com.github.raffaelliscandiffio.controller;

import java.util.ArrayList;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.view.TotemView;

public class TotemController {

	private PurchaseBroker broker;
	private TotemView totemView;
	private Order order;

	public TotemController(PurchaseBroker broker, TotemView totemView) {
		this.broker = broker;
		this.totemView = totemView;
	}

	public void startShopping() {
		if (order == null)
			order = new Order(new ArrayList<>());
		totemView.showShopping();
		totemView.showAllProducts(broker.getAllProducts());
	}

	Order getOrder() {
		return this.order;
	}

	void setOrder(Order order) {
		this.order = order;
	}
}