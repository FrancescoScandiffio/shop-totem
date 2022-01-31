package com.github.raffaelliscandiffio.repository.mysql;

import java.util.List;
import java.util.NoSuchElementException;

import javax.persistence.EntityManager;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.ProductRepository;

public class ProductMySQLRepository implements ProductRepository{
	
    private EntityManager entityManager;
	
	public ProductMySQLRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Product> findAll() {
		return entityManager.createQuery("select p from Product p", Product.class).getResultList();
	}

	@Override
	public Product findById(long id) {
		Product product = entityManager.find(Product.class, id);
		if (product != null)
			return product;
		else
			throw new NoSuchElementException(String.format("Product with id %d not found", id));
	}

	@Override
	public void save(Product product) {
		// TODO Auto-generated method stub
		
	}
}
