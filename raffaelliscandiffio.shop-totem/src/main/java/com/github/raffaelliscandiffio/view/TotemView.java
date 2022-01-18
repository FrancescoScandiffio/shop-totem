package com.github.raffaelliscandiffio.view;

import java.util.List;

import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;

public interface TotemView {

	void showShopping();

	void showAllProducts(List<Product> allProducts);

	void allItemsRemoved();

	void showWelcome();

	void itemAdded(OrderItem item);

	void showMessage(String msg);

	void showWarning(String msg);

	void itemModified(OrderItem old, OrderItem modified);

	void itemRemoved(OrderItem item);

	void showErrorItemNotFound(String msg, OrderItem item);

	void showErrorProductNotFound(String msg, Product product);

	void showErrorMessage(String msg);

	void clearOrderList();

	void showGoodbye();

	void showErrorEmptyOrder(String msg);

	void showOrder();
}
