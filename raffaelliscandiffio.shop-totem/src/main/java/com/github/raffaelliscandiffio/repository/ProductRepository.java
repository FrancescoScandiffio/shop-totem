package com.github.raffaelliscandiffio.repository;

import java.util.List;

import com.github.raffaelliscandiffio.model.Product;

public interface ProductRepository {
	public List<Product> findAll();

	public Product findById(String id);

	public void save(Product product);

}
