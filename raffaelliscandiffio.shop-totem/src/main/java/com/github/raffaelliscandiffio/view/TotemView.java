package com.github.raffaelliscandiffio.view;

import java.util.List;

import com.github.raffaelliscandiffio.model.Product;

public interface TotemView {

	public void showProducts(List<Product> products);
	
	// TODO shows message OK insert with "name product" and "quantity" inserted in cart
	public void notifyCorrectInsert(long productId, int quantityInserted);
	
	// TODO shows message ALMOST OK with "name product" and "quantity" inserted in cart
	public void notifyPartialInsert(long productId, int quantityInserted);
	
	// TODO shows message No insert in cart with "name product"
	public void notifyNoInsert(long productId);
	
	// TODO shows message Error occurred: no existing product 
	public void notifyNoExistingProduct(long productId);
	
	public void removeProduct(long productId);
	
	public void addProduct(Product product);
}
