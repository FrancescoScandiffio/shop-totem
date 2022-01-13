package com.github.raffaelliscandiffio.view;

import java.util.List;

import com.github.raffaelliscandiffio.model.Product;

public interface TotemView {

	void showShopping();

	void showAllProducts(List<Product> allProducts);

	void allItemsRemoved();

	void showWelcome();

}
