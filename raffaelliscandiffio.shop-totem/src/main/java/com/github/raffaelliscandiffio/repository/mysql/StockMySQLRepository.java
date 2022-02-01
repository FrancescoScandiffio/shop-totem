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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(Stock stock) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Stock stock) {
		// TODO Auto-generated method stub

	}

}
