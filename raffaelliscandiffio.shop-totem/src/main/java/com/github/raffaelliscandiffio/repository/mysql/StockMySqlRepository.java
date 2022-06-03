package com.github.raffaelliscandiffio.repository.mysql;

import javax.persistence.EntityManager;

import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.StockRepository;

public class StockMySqlRepository implements StockRepository {

	private EntityManager entityManager;

	public StockMySqlRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Stock findById(String id) {
		return entityManager.find(Stock.class, id);
	}

	@Override
	public void save(Stock stock) {
		entityManager.persist(stock);
	}

	@Override
	public void update(Stock stock) {
		entityManager.merge(stock);

	}
}