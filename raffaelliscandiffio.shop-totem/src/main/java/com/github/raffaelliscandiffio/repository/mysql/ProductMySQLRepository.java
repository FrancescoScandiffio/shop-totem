package com.github.raffaelliscandiffio.repository.mysql;

import java.util.List;

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
		return entityManager.find(Product.class, id);
	}

	@Override
	public void save(Product product) {
		entityManager.getTransaction().begin();
		entityManager.persist(product);
		entityManager.getTransaction().commit();
	}
}
