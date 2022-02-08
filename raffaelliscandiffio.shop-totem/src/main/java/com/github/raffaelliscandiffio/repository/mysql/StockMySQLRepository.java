package com.github.raffaelliscandiffio.repository.mysql;

import javax.persistence.EntityManager;

import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.StockRepository;

public class StockMySQLRepository implements StockRepository {

	private EntityManager entityManager;

	public StockMySQLRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Stock findById(long id) {
		return entityManager.find(Stock.class, id);
	}

	@Override
	public void save(Stock stock) {
		entityManager.getTransaction().begin();
		entityManager.persist(stock);
		entityManager.getTransaction().commit();
	}

	@Override
	public void update(Stock stock) {
		entityManager.getTransaction().begin();
		entityManager.merge(stock);
		entityManager.getTransaction().commit();
	}
}