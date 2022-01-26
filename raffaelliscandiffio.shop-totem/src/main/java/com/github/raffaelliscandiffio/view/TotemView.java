package com.github.raffaelliscandiffio.view;

import java.util.List;

import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;

public interface TotemView {

	void showShopping();

	void showAllProducts(List<Product> allProducts);

	void showWelcome();

	void itemAdded(OrderItem item);

	void showShoppingMessage(String msg);

	void showCartMessage(String msg);

	void showCartErrorMessage(String msg);

	void showShoppingErrorMessage(String msg);

	void showWarning(String msg);

	void itemModified(OrderItem old, OrderItem modified);

	void itemRemoved(OrderItem item);

	void showErrorItemNotFound(String msg, OrderItem item);

	void showErrorProductNotFound(String msg, Product product);

	void allItemsRemoved();

	void showGoodbye();

	void showErrorEmptyOrder(String msg);

	void showOrder();

}