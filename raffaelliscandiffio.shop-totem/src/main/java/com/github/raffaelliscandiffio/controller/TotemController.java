package com.github.raffaelliscandiffio.controller;

import java.util.List;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.view.TotemView;
import com.github.raffaelliscandiffio.transaction.TransactionException;


public class TotemController {
	
	private ShoppingService shoppingService;
	private TotemView totemView;
	
	public TotemController(ShoppingService shoppingService, TotemView totemView) {
		this.shoppingService = shoppingService;
		this.totemView = totemView; 
	}
	
	public void startShopping() {
		Order order = new Order(OrderStatus.OPEN);
		try {
			order = shoppingService.saveOrder(order);
			List<Product> allProducts = shoppingService.getAllProducts();
			
			totemView.resetView();
			totemView.resetLabels();
			totemView.setOrderId(order.getId());
			totemView.showShopping();
			totemView.showAllProducts(allProducts);
			
		} catch(TransactionException e){
			totemView.resetView();
			totemView.showShopping();
			totemView.showShoppingErrorMessage(e.getMessage());
		}
	}
	
	public void openShopping() {
		try {
			List<Product> allProducts = shoppingService.getAllProducts();
			totemView.showShopping();
			totemView.showAllProducts(allProducts);
		} catch(TransactionException e) {
			totemView.showShopping();
			totemView.showShoppingErrorMessage(e.getMessage());
		}
	}
	
	public void cancelShopping(String orderId) {
		try {
			shoppingService.deleteOrder(orderId);
			totemView.resetView();
			totemView.resetLabels();
			totemView.setOrderId(null);
			totemView.showWelcome();
		}catch(TransactionException e) {
			totemView.showCartErrorMessage(e.getMessage());
		}
	}
	
	public void openOrder() {
		totemView.showOrder();
	}
	
	public void buyProduct(String orderId, String productId, int quantity) {
		
		try {
			OrderItem orderItem = shoppingService.buyProduct(orderId, productId, quantity);
			totemView.itemAdded(orderItem);
			totemView.showShoppingMessage("Added " + quantity + " " + orderItem.getProduct().getName());
		}catch(TransactionException e) {
			totemView.showShoppingErrorMessage(e.getMessage());
		}	
	}
	
	public void removeItem(OrderItem orderItem) {
		try {
			shoppingService.deleteItem(orderItem);
			totemView.itemRemoved(orderItem);
			totemView.showCartMessage("Removed all " + orderItem.getProduct().getName());
		} catch(TransactionException e) {
			totemView.resetView();
			try {
				List<Product> allProducts = shoppingService.getAllProducts();
				List<OrderItem> allOrderItems = shoppingService.getOrderItems(totemView.getOrderId());
				totemView.showAllProducts(allProducts);
				totemView.showAllOrderItems(allOrderItems);
				totemView.showCartErrorMessage(e.getMessage());
			}catch(TransactionException ee) {
				totemView.showCartErrorMessage(ee.getMessage());
			}
		}
	}
	
	public void returnItem(OrderItem itemToReturn, int quantity) {
		try {
			OrderItem modifiedItem = shoppingService.returnItem(itemToReturn, quantity);
			totemView.itemModified(itemToReturn, modifiedItem);
			totemView.showCartMessage("Removed " + quantity + " " + modifiedItem.getProduct().getName());
		}catch(TransactionException e) {
			totemView.resetView();
			try {
				List<Product> allProducts = shoppingService.getAllProducts();
				List<OrderItem> allOrderItems = shoppingService.getOrderItems(totemView.getOrderId());
				totemView.showAllProducts(allProducts);
				totemView.showAllOrderItems(allOrderItems);
				totemView.showCartErrorMessage(e.getMessage());
			}catch(TransactionException ee) {
				totemView.showCartErrorMessage(ee.getMessage());
			}
		}
	}
	
	public void checkout(String orderId) {
		try {
			shoppingService.closeOrder(orderId);
			totemView.setOrderId(null);
			totemView.resetView();
			totemView.resetLabels();
			totemView.showGoodbye();
		} catch(TransactionException e) {
			totemView.showCartErrorMessage(e.getMessage());
		}
	}
	
}
