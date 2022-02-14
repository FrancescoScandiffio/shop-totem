package com.github.raffaelliscandiffio.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.OrderRepository;
import com.github.raffaelliscandiffio.view.TotemView;

public class TotemController {

	private PurchaseBroker broker;
	private TotemView totemView;
	private Order order;
	private boolean firstLoading;
	private OrderRepository orderRepository;

	public TotemController(PurchaseBroker broker, TotemView totemView, OrderRepository orderRepository) {
		this.broker = broker;
		this.totemView = totemView;
		this.firstLoading = true;
		this.orderRepository = orderRepository;
	}

	public void startShopping() {
		if (order == null)
			order = new Order(new ArrayList<>());
		if (firstLoading) {
			totemView.showAllProducts(broker.retrieveProducts());
			firstLoading = false;
		}
		totemView.showShopping();
	}

	public void openShopping() {
		totemView.showShopping();
	}

	public void openOrder() {
		totemView.showOrder();
	}

	public void buyProduct(Product product, int requested) {
		if (requested <= 0) {
			totemView.showShoppingErrorMessage("Buy quantity must be positive: received " + requested);
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
		OrderItem modifiedItem = order.increaseProductQuantity(product, provided);
		if (storedItem == null)
			totemView.itemAdded(modifiedItem);
		else
			totemView.itemModified(storedItem, modifiedItem);

		if (provided < requested)
			totemView.showWarning("Not enough " + product.getName() + " in stock: added only " + provided);
		else
			totemView.showShoppingMessage("Added " + requested + " " + product.getName());

	}

	public void removeItem(OrderItem item) {
		try {
			order.popItemById(item.getId());
			broker.returnProduct(item.getProduct().getId(), item.getQuantity());
			totemView.itemRemoved(item);
			totemView.showCartMessage("Removed all " + item.getProduct().getName());
		} catch (NoSuchElementException exception) {
			totemView.showErrorItemNotFound("Item not found", item);
		}
	}

	public void returnProduct(OrderItem item, int quantity) {
		try {
			OrderItem modifiedItem = order.decreaseProductQuantity(item.getId(), quantity);
			broker.returnProduct(item.getProduct().getId(), quantity);
			totemView.itemModified(item, modifiedItem);
			totemView.showCartMessage("Removed " + quantity + " " + item.getProduct().getName());
		} catch (NoSuchElementException exception) {
			totemView.showErrorItemNotFound("Item not found", item);
		} catch (IllegalArgumentException exception) {
			totemView.showCartErrorMessage(exception.getMessage());
		}
	}

	// TODO after returnProduct is implemented: check if a try-catch is needed here
	public void cancelShopping() {
		List<OrderItem> items = order.getItems();
		if (!items.isEmpty()) {
			order.clear();
			totemView.allItemsRemoved();
			for (OrderItem item : items) {
				broker.returnProduct(item.getProduct().getId(), item.getQuantity());
			}
		}
		totemView.showWelcome();
	}

	public void confirmOrder() {
		if (order.getItems().isEmpty()) {
			totemView.showErrorEmptyOrder("Cannot confirm an empty order");
			return;
		}

		orderRepository.save(order);
		totemView.showGoodbye();
		totemView.allItemsRemoved();
		order = null;
	}

	Order getOrder() {
		return this.order;
	}

	void setOrder(Order order) {
		this.order = order;
	}

	boolean isFirstLoading() {
		return firstLoading;
	}

	void setFirstLoading(boolean firstLoading) {
		this.firstLoading = firstLoading;
	}

}