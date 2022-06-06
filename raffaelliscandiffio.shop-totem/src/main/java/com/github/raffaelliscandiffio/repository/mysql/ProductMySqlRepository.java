package com.github.raffaelliscandiffio.repository.mysql;

import java.util.List;

import javax.persistence.EntityManager;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.ProductRepository;

public class ProductMySqlRepository implements ProductRepository {

	private EntityManager entityManager;

	public ProductMySqlRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void save(Product product) {
		entityManager.persist(product);
	}

	@Override
	public Product findById(String id) {
		return entityManager.find(Product.class, id);
	}

	@Override
	public List<Product> findAll() {
		return entityManager.createQuery("select p from Product p", Product.class).getResultList();
	}

}
