package com.github.raffaelliscandiffio.repository.mysql;

import javax.persistence.EntityManager;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.repository.OrderRepository;

public class OrderMySqlRepository implements OrderRepository {

	private EntityManager entityManager;

	public OrderMySqlRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void save(Order order) {
		entityManager.persist(order);
	}

	@Override
	public void update(Order order) {
		entityManager.merge(order);
	}

	@Override
	public Order findById(String id) {
		return entityManager.find(Order.class, id);
	}

	@Override
	public void delete(String id) {
		entityManager.createQuery("DELETE FROM Order where id=:order_id").setParameter("order_id", id).executeUpdate();
	}

}
