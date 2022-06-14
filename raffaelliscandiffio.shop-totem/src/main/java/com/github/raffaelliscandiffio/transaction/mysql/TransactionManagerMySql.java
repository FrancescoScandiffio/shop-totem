package com.github.raffaelliscandiffio.transaction.mysql;

import javax.persistence.EntityManager;

import com.github.raffaelliscandiffio.exception.TransactionException;
import com.github.raffaelliscandiffio.repository.mysql.OrderItemMySqlRepository;
import com.github.raffaelliscandiffio.repository.mysql.OrderMySqlRepository;
import com.github.raffaelliscandiffio.repository.mysql.ProductMySqlRepository;
import com.github.raffaelliscandiffio.repository.mysql.StockMySqlRepository;
import com.github.raffaelliscandiffio.transaction.TransactionCode;
import com.github.raffaelliscandiffio.transaction.TransactionManager;

public class TransactionManagerMySql implements TransactionManager {

	private EntityManager entityManager;

	public TransactionManagerMySql(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public <T> T runInTransaction(TransactionCode<T> code) {
		try {
			entityManager.getTransaction().begin();
			T result = code.apply(new ProductMySqlRepository(entityManager), new StockMySqlRepository(entityManager),
					new OrderMySqlRepository(entityManager), new OrderItemMySqlRepository(entityManager));
			entityManager.getTransaction().commit();
			return result;
		} catch (Exception e) {
			entityManager.getTransaction().rollback();
			throw new TransactionException(e.getMessage());
		}
	}
}
