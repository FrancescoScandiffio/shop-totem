package com.github.raffaelliscandiffio.repository.mysql;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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

	@Override
	public Stock findByProductId(String productId) {
		TypedQuery<Stock> query = entityManager
				.createQuery("SELECT s FROM Stock s WHERE s.product.id = :productId", Stock.class)
				.setParameter("productId", productId);
		Stock s = query.getResultList().stream().findFirst().orElse(null);
		if (s != null)
			entityManager.refresh(s);
		return s;
	}
}