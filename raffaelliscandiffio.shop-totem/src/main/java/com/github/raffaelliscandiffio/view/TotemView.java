package com.github.raffaelliscandiffio.view;

import java.util.List;

import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;

public interface TotemView {

	void showShopping();

	void showAllProducts(List<Product> allProducts);
	
	void showAllOrderItems(List<OrderItem> allOrderItems);

	void showWelcome();

	void itemAdded(OrderItem item);

	void showShoppingMessage(String msg);

	void showCartMessage(String msg);

	void showCartErrorMessage(String msg);

	void showShoppingErrorMessage(String msg);

	void itemModified(OrderItem old, OrderItem modified);

	void itemRemoved(OrderItem item);

	void showGoodbye();

	void showOrder();
	
	void setOrderId(String orderId);
	
	String getOrderId();

	void resetView();
	
	void resetLabels();

}