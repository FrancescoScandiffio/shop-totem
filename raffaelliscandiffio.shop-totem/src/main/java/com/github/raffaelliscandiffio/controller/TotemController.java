package com.github.raffaelliscandiffio.controller;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
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
		totemView.showAllProducts(broker.retrieveProducts());
	}

	public void buyProduct(Product product, int requested) {
		if (requested <= 0) {
			totemView.showErrorMessage("Buy quantity must be positive: received " + requested);
			return;
		}

		if (!broker.doesProductExist(product.getId())) {
			totemView.showErrorProductNotFound("Product not found", product);
			return;
		}

		int provided = broker.takeAvailable(product.getId(), requested);

		if (provided == 0) {
			totemView.showWarning("Item out of stock: " + product.getName());
			return;
		}

		OrderItem storedItem = order.findItemByProductId(product.getId());
		OrderItem modifiedItem = order.insertItem(product, provided);
		if (storedItem == null)
			totemView.itemAdded(modifiedItem);
		else
			totemView.itemModified(storedItem, modifiedItem);

		if (provided < requested)
			totemView.showWarning("Not enough " + product.getName() + " in stock: added only " + provided);
		else
			totemView.showMessage("Added " + requested + " " + product.getName());

	}

	public void removeItem(OrderItem item) {
		try {
			order.popItemById(item.getId());
			broker.returnProduct(item.getProduct().getId(), item.getQuantity());
			totemView.itemRemoved(item);
			totemView.showMessage("Removed all " + item.getProduct().getName());
		} catch (NoSuchElementException exception) {
			totemView.showErrorItemNotFound("Item not found", item);
		}
	}

	public void returnProduct(OrderItem item, int quantity) {
		try {
			OrderItem modifiedItem = order.decreaseItem(item.getId(), quantity);
			broker.returnProduct(item.getProduct().getId(), quantity);
			totemView.itemModified(item, modifiedItem);
			totemView.showMessage("Removed " + quantity + " " + item.getProduct().getName());
		} catch (NoSuchElementException exception) {
			totemView.showErrorItemNotFound("Item not found", item);
		} catch (IllegalArgumentException exception) {
			totemView.showErrorMessage(exception.getMessage());
		}
	}

	Order getOrder() {
		return this.order;
	}

	void setOrder(Order order) {
		this.order = order;
	}
}