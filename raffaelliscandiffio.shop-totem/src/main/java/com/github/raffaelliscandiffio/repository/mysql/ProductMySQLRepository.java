package com.github.raffaelliscandiffio.repository.mysql;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.utils.LogUtility;

public class ProductMySQLRepository implements ProductRepository{
	
    private EntityManager entityManager;
    private static final Logger LOGGER = LogManager.getLogger(ProductMySQLRepository.class);
	private static final LogUtility logUtil = new LogUtility();
	
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
		return null;
	}

	@Override
	public void save(Product product) {
		try {
			entityManager.getTransaction().begin();
			entityManager.persist(product);
			entityManager.getTransaction().commit();
		}catch(EntityExistsException e) {
			LOGGER.log(Level.ERROR, "Product with id {} already in database \n{}", product.getId(),
					logUtil.getReducedStackTrace(e));
		}
	}
}
